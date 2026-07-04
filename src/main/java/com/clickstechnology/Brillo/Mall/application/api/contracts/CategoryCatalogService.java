package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.category.CategoryDto;
import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;

import java.util.List;

public interface CategoryCatalogService {

    List<CategoryDto> listCategories(BusinessCategory type);

    default List<CategoryDto> listProductCategories() {
        return listCategories(BusinessCategory.PRODUCTS);
    }

    String resolveCategory(BusinessCategory type, String value);
}
