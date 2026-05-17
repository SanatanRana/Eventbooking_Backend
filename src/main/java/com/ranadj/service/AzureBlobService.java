package com.ranadj.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface AzureBlobService {
    String uploadFile(MultipartFile file) throws IOException;
    void deleteFile(String fileUrl);
}
