package com.alumniconnect.backend.controller;

import com.alumniconnect.backend.dto.common.UploadResponse;
import com.alumniconnect.backend.service.FileStorageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(name = "Files", description = "Serving and uploading images (profile photos, post images)")
public class FileController {

    private final FileStorageService fileStorageService;

    /** Public (see SecurityConfig): plain &lt;img&gt; tags can't send a bearer token. Names are random UUIDs. */
    @GetMapping("/{folder}/{filename:.+}")
    public ResponseEntity<Resource> serve(@PathVariable String folder, @PathVariable String filename) {
        Resource resource = fileStorageService.load(folder, filename);
        return ResponseEntity.ok()
                .contentType(fileStorageService.mediaTypeFor(filename))
                .header("Cache-Control", "public, max-age=31536000, immutable")
                .header("X-Content-Type-Options", "nosniff")
                .body(resource);
    }

    /** Uploads an image for a feed post; the returned url goes into the post's imageUrl. */
    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadPostImage(@RequestParam("file") MultipartFile file) {
        String url = fileStorageService.storeImage(file, FileStorageService.POSTS);
        return ResponseEntity.ok(UploadResponse.builder().url(url).build());
    }
}
