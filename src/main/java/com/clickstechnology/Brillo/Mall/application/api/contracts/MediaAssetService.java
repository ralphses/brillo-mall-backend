package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.enums.FileType;
import org.springframework.web.multipart.MultipartFile;

public interface MediaAssetService {
    String uploadFile(FileType fileType, MultipartFile logoFile, String businessId);
}
