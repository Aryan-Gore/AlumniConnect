package com.alumniconnect.backend.service;

import com.alumniconnect.backend.dto.common.UploadResponse;
import com.alumniconnect.backend.entity.User;
import com.alumniconnect.backend.exception.ResourceNotFoundException;
import com.alumniconnect.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** One avatar per account, whatever the role — chat, feed and directory all read users.profile_photo_url. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfilePhotoService {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public UploadResponse upload(Long userId, MultipartFile file) {
        User user = requireUser(userId);
        String previous = user.getProfilePhotoUrl();

        String url = fileStorageService.storeImage(file, FileStorageService.PROFILES);
        user.setProfilePhotoUrl(url);
        userRepository.save(user);

        fileStorageService.deleteAfterCommit(previous);
        return UploadResponse.builder().url(url).build();
    }

    @Transactional
    public void remove(Long userId) {
        User user = requireUser(userId);
        String previous = user.getProfilePhotoUrl();
        user.setProfilePhotoUrl(null);
        userRepository.save(user);
        fileStorageService.deleteAfterCommit(previous);
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found."));
    }
}
