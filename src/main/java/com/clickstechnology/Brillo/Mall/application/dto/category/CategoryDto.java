package com.clickstechnology.Brillo.Mall.application.dto.category;

import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    private String code;
    private String label;
    private BusinessCategory type;
}
