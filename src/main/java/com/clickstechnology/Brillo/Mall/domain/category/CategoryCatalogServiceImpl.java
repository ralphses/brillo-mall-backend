package com.clickstechnology.Brillo.Mall.domain.category;

import com.clickstechnology.Brillo.Mall.application.api.contracts.CategoryCatalogService;
import com.clickstechnology.Brillo.Mall.application.dto.category.CategoryDto;
import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.infrastructure.persistence.JpaAuditor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
class CategoryCatalogServiceImpl implements CategoryCatalogService {

    private final CategoryCatalogRepository categoryCatalogRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> listCategories(BusinessCategory type) {
        return categoryCatalogRepository
                .findAllByCategoryTypeAndRecordStatusOrderByDisplayOrderAscLabelAsc(
                        type,
                        JpaAuditor.RecordStatus.ACTIVE)
                .stream()
                .map(CategoryCatalog::dto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public String resolveCategory(BusinessCategory type, String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(type.name().toLowerCase() + " category cannot be blank.");
        }

        return categoryCatalogRepository
                .findAllByCategoryTypeAndRecordStatusOrderByDisplayOrderAscLabelAsc(
                        type,
                        JpaAuditor.RecordStatus.ACTIVE)
                .stream()
                .filter(category -> category.matches(value))
                .findFirst()
                .map(CategoryCatalog::getCode)
                .orElseThrow(() -> new BusinessException(
                        "Invalid " + type.name().toLowerCase() + " category: " + value));
    }
}
