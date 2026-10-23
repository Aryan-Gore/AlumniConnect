package com.alumniconnect.backend.service;

import com.alumniconnect.backend.exception.BadRequestException;
import com.alumniconnect.backend.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Stores uploaded images on local disk (mount a volume at app.storage.dir in
 * Docker). Three rules keep this safe:
 *  1. the file type is decided from the file's own first bytes, never from the
 *     client's Content-Type or file name;
 *  2. the stored name is a random UUID plus an extension WE choose, so no
 *     client-supplied text ever reaches the file system;
 *  3. reads accept only that exact UUID.ext shape inside two fixed folders.
 */
@Slf4j
@Service
public class FileStorageService {

    public static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    public static final String PROFILES = "profiles";
    public static final String POSTS = "posts";

    private static final Set<String> FOLDERS = Set.of(PROFILES, POSTS);
    private static final Pattern STORED_NAME = Pattern.compile(
            "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|png|gif|webp)$");
    private static final String URL_PREFIX = "/files/";

    private final Path root;

    public FileStorageService(@Value("${app.storage.dir}") String storageDir) {
        this.root = Paths.get(storageDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    void createFolders() {
        try {
            for (String folder : FOLDERS) {
                Files.createDirectories(root.resolve(folder));
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot create the upload directory " + root, ex);
        }
    }

    /** Validates and stores an image; returns its public path, e.g. {@code /files/profiles/<uuid>.jpg}. */
    public String storeImage(MultipartFile file, String folder) {
        if (!FOLDERS.contains(folder)) {
            throw new IllegalArgumentException("Unknown upload folder: " + folder);
        }
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Choose an image to upload.");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new BadRequestException("Images can be at most 5 MB.");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException ex) {
            throw new BadRequestException("Couldn't read the uploaded file.");
        }

        String extension = detectExtension(bytes);
        if (extension == null) {
            throw new BadRequestException("Only JPEG, PNG, GIF or WebP images are allowed.");
        }

        String storedName = UUID.randomUUID() + "." + extension;
        Path target = root.resolve(folder).resolve(storedName).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalStateException("Resolved upload path escaped the storage root");
        }
        try {
            Files.write(target, bytes);
        } catch (IOException ex) {
            log.error("Could not write upload to {}", target, ex);
            throw new IllegalStateException("Could not save the uploaded file", ex);
        }
        return URL_PREFIX + folder + "/" + storedName;
    }

    /** Resolves a stored file for download; unknown folders and names that aren't our own UUID.ext shape are simply "not found". */
    public Resource load(String folder, String filename) {
        if (!FOLDERS.contains(folder) || filename == null || !STORED_NAME.matcher(filename).matches()) {
            throw new ResourceNotFoundException("File not found.");
        }
        Path path = root.resolve(folder).resolve(filename).normalize();
        if (!path.startsWith(root) || !Files.isRegularFile(path)) {
            throw new ResourceNotFoundException("File not found.");
        }
        return new FileSystemResource(path);
    }

    /**
     * True only for the exact public path of an image this service stored in {@code folder}
     * (e.g. {@code /files/posts/<uuid>.jpg}) whose file is still on disk. Lets a post prove that the
     * image URL it was handed really came from an upload, instead of trusting any string.
     */
    public boolean isStoredImage(String url, String folder) {
        if (url == null || folder == null || !FOLDERS.contains(folder)) {
            return false;
        }
        String prefix = URL_PREFIX + folder + "/";
        if (!url.startsWith(prefix)) {
            return false;
        }
        String filename = url.substring(prefix.length());
        if (!STORED_NAME.matcher(filename).matches()) {
            return false;
        }
        Path path = root.resolve(folder).resolve(filename).normalize();
        return path.startsWith(root) && Files.isRegularFile(path);
    }

    public MediaType mediaTypeFor(String filename) {
        if (filename.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (filename.endsWith(".gif")) return MediaType.IMAGE_GIF;
        if (filename.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        return MediaType.IMAGE_JPEG;
    }

    /** Best-effort removal of a file we stored earlier (old avatar, deleted post image). Never throws. */
    public void deleteQuietly(String url) {
        if (url == null || !url.startsWith(URL_PREFIX)) return;
        String[] parts = url.substring(URL_PREFIX.length()).split("/");
        if (parts.length != 2) return;
        try {
            Resource resource = load(parts[0], parts[1]);
            Files.deleteIfExists(resource.getFile().toPath());
        } catch (Exception ex) {
            log.debug("Could not delete {}: {}", url, ex.getMessage());
        }
    }

    /**
     * Deletes the file once the surrounding transaction has committed — so a
     * rolled-back "replace my photo" never leaves the database pointing at a
     * file we already removed. Outside a transaction it deletes immediately.
     */
    public void deleteAfterCommit(String url) {
        if (url == null) return;
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    deleteQuietly(url);
                }
            });
        } else {
            deleteQuietly(url);
        }
    }

    private static String detectExtension(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
            return "png";
        }
        if (b.length >= 6 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8') {
            return "gif";
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "webp";
        }
        return null;
    }
}
