package com.ranadj.controller;

import com.ranadj.entity.DjMedia;
import com.ranadj.entity.User;
import com.ranadj.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    @Autowired
    private MediaService mediaService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadMedia(@RequestParam("file") MultipartFile file,
                                         @RequestParam(value = "description", required = false) String description,
                                         @AuthenticationPrincipal User user) {
        try {
            DjMedia uploadedMedia = mediaService.uploadMedia(file, description, user.getId());
            return ResponseEntity.ok(uploadedMedia);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload media: " + e.getMessage());
        }
    }

    @GetMapping("/admin/{adminId}")
    public ResponseEntity<List<DjMedia>> getMediaByAdmin(@PathVariable Long adminId) {
        List<DjMedia> media = mediaService.getMediaByAdmin(adminId);
        return ResponseEntity.ok(media);
    }

    @DeleteMapping("/{mediaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteMedia(@PathVariable Long mediaId) {
        try {
            mediaService.deleteMedia(mediaId);
            return ResponseEntity.ok("Media deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete media: " + e.getMessage());
        }
    }
}
