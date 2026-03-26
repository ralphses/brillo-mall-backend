package com.clickstechnology.Brillo.Mall.infrastructure.external;

import com.clickstechnology.Brillo.Mall.application.api.contracts.MediaAssetService;
import com.clickstechnology.Brillo.Mall.application.enums.FileType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
class CloudinaryMediaService implements MediaAssetService {
    @Override
    public String uploadFile(FileType fileType, MultipartFile logoFile, String businessId) {
        return "url";
    }
}
