package com.clickstechnology.Brillo.Mall.domain.category;

import com.clickstechnology.Brillo.Mall.application.dto.category.CategoryDto;
import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryCatalogServiceImplTest {

    @Mock
    private CategoryCatalogRepository categoryCatalogRepository;

    @InjectMocks
    private CategoryCatalogServiceImpl categoryCatalogService;

    @Test
    void listCategories_shouldReturnMappedCategories() {
        CategoryCatalog category = CategoryCatalog.builder()
                .code("PHARMACY")
                .label("Pharmacy")
                .categoryType(BusinessCategory.PRODUCTS)
                .displayOrder(1)
                .build();

        when(categoryCatalogRepository.findAllByCategoryTypeAndRecordStatusOrderByDisplayOrderAscLabelAsc(
                BusinessCategory.PRODUCTS,
                com.clickstechnology.Brillo.Mall.infrastructure.persistence.JpaAuditor.RecordStatus.ACTIVE))
                .thenReturn(List.of(category));

        List<CategoryDto> result = categoryCatalogService.listCategories(BusinessCategory.PRODUCTS);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("PHARMACY");
        assertThat(result.get(0).getLabel()).isEqualTo("Pharmacy");
        assertThat(result.get(0).getType()).isEqualTo(BusinessCategory.PRODUCTS);
    }

    @Test
    void resolveCategory_shouldAcceptCodeOrLabel() {
        CategoryCatalog category = CategoryCatalog.builder()
                .code("PHARMACY")
                .label("Pharmacy")
                .categoryType(BusinessCategory.PRODUCTS)
                .displayOrder(1)
                .build();

        when(categoryCatalogRepository.findAllByCategoryTypeAndRecordStatusOrderByDisplayOrderAscLabelAsc(
                BusinessCategory.PRODUCTS,
                com.clickstechnology.Brillo.Mall.infrastructure.persistence.JpaAuditor.RecordStatus.ACTIVE))
                .thenReturn(List.of(category));

        assertThat(categoryCatalogService.resolveCategory(BusinessCategory.PRODUCTS, "pharmacy")).isEqualTo("PHARMACY");
        assertThat(categoryCatalogService.resolveCategory(BusinessCategory.PRODUCTS, "Pharmacy")).isEqualTo("PHARMACY");
    }

    @Test
    void resolveCategory_shouldThrowWhenMissing() {
        when(categoryCatalogRepository.findAllByCategoryTypeAndRecordStatusOrderByDisplayOrderAscLabelAsc(
                BusinessCategory.SERVICES,
                com.clickstechnology.Brillo.Mall.infrastructure.persistence.JpaAuditor.RecordStatus.ACTIVE))
                .thenReturn(List.of());

        assertThatThrownBy(() -> categoryCatalogService.resolveCategory(BusinessCategory.SERVICES, "Unknown"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid services category");
    }
}
