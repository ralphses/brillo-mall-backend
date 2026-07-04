package com.clickstechnology.Brillo.Mall.domain.category;

import com.clickstechnology.Brillo.Mall.application.dto.category.CategoryDto;
import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;
import com.clickstechnology.Brillo.Mall.infrastructure.persistence.JpaAuditor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("record_status <> 'DELETED'")
@Table(
        name = "brillo_category_catalog",
        indexes = {
                @Index(name = "idx_brillo_category_catalog_type", columnList = "category_type"),
                @Index(name = "idx_brillo_category_catalog_code", columnList = "code")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_brillo_category_catalog_type_code", columnNames = {"category_type", "code"})
        }
)
class CategoryCatalog extends JpaAuditor implements Serializable {

    @Column(name = "code", length = 100, nullable = false)
    private String code;

    @Column(name = "label", length = 150, nullable = false)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", length = 20, nullable = false)
    private BusinessCategory categoryType;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    public boolean matches(String value) {
        if (value == null) {
            return false;
        }
        return code.equalsIgnoreCase(value.trim()) || label.equalsIgnoreCase(value.trim());
    }

    public CategoryDto dto() {
        return CategoryDto.builder()
                .code(code)
                .label(label)
                .type(categoryType)
                .build();
    }
}
