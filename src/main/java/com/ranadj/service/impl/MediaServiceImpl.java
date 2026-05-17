package com.ranadj.service.impl;

import com.ranadj.entity.DjMedia;
import com.ranadj.entity.MediaType;
import com.ranadj.entity.User;
import com.ranadj.repository.DjMediaRepository;
import com.ranadj.repository.UserRepository;
import com.ranadj.service.AzureBlobService;
import com.ranadj.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class MediaServiceImpl implements MediaService {

    @Autowired
    private DjMediaRepository mediaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AzureBlobService azureBlobService;

    @Override
    public DjMedia uploadMedia(MultipartFile file, String description, Long adminId) throws IOException {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with ID: " + adminId));

        String mediaUrl = azureBlobService.uploadFile(file);

        MediaType mediaType = determineMediaType(file.getContentType());

        DjMedia djMedia = DjMedia.builder()
                .mediaUrl(mediaUrl)
                .mediaType(mediaType)
                .description(description)
                .admin(admin)
                .build();

        return mediaRepository.save(djMedia);
    }

    @Override
    public List<DjMedia> getMediaByAdmin(Long adminId) {
        return mediaRepository.findByAdminId(adminId);
    }

    @Override
    public void deleteMedia(Long mediaId) {
        DjMedia djMedia = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("Media not found with ID: " + mediaId));
        
        azureBlobService.deleteFile(djMedia.getMediaUrl());
        mediaRepository.delete(djMedia);
    }

    private MediaType determineMediaType(String contentType) {
        if (contentType != null && contentType.startsWith("video")) {
            return MediaType.VIDEO;
        }
        return MediaType.IMAGE;
    }
}
