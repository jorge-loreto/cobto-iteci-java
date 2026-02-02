package com.iteci.cobro.services;


import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;

@Service
public class GcsStorageService {

    private final Storage storage;
    @Value("${gcp.bucket-name}")
    private String bucketName;

    public GcsStorageService(Storage storage) {
        this.storage = storage;
    }

    public void uploadPdf(byte[] pdfBytes, String objectName) {
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("application/pdf")
                .build();
        storage.create(blobInfo, pdfBytes);
    }
}
