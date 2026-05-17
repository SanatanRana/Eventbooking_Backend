package com.ranadj.service.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.ranadj.service.AzureBlobService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.UUID;

@Service
public class AzureBlobServiceImpl implements AzureBlobService {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.container-name}")
    private String containerName;

    private BlobContainerClient containerClient;

    @PostConstruct
    public void init() {
        try {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
            containerClient = blobServiceClient.getBlobContainerClient(containerName);
            if (!containerClient.exists()) {
                containerClient.create();
            }
        } catch (Exception e) {
            // Log warning if Azure is not fully configured, allowing app to start without crashing
            System.err.println("Failed to initialize Azure Blob Storage Client: " + e.getMessage());
        }
    }

    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;

        BlobClient blobClient = containerClient.getBlobClient(uniqueFilename);
        blobClient.upload(file.getInputStream(), file.getSize(), true);

        return blobClient.getBlobUrl();
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            // Extract blob name from URL
            String[] parts = fileUrl.split("/");
            String blobName = parts[parts.length - 1];
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            blobClient.delete();
        } catch (Exception e) {
            System.err.println("Failed to delete file from Azure Blob Storage: " + e.getMessage());
        }
    }
}
