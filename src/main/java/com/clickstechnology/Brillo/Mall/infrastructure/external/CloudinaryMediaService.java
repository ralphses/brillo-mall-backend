package com.clickstechnology.Brillo.Mall.infrastructure.external;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.clickstechnology.Brillo.Mall.application.api.contracts.MediaAssetService;
import com.clickstechnology.Brillo.Mall.application.enums.FileType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
class CloudinaryMediaService implements MediaAssetService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadFile(FileType fileType, MultipartFile logoFile, String businessId) {
        try {
            String folder = "Brillo-Mall/" + fileType.name().toLowerCase();
            String publicId = businessId + "_" + UUID.randomUUID();

            Map<?, ?> uploadOptions = ObjectUtils.asMap(
                    "public_id", publicId,
                    "folder", folder,
                    "overwrite", true,
                    "resource_type", "auto"
            );

            Map<?, ?> uploadResult = cloudinary.uploader().upload(logoFile.getBytes(), uploadOptions);

            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("File uploaded successfully to Cloudinary. URL: {}", secureUrl);

            return secureUrl;
        } catch (IOException e) {
            log.error("Error uploading file to Cloudinary for businessId: {}", businessId, e);
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }
}