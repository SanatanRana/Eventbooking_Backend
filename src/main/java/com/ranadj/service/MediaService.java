package com.ranadj.service;

import com.ranadj.entity.DjMedia;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MediaService {
    DjMedia uploadMedia(MultipartFile file, String description, Long adminId) throws IOException;
    List<DjMedia> getMediaByAdmin(Long adminId);
    void deleteMedia(Long mediaId);
}
