package com.clickstechnology.Brillo.Mall.domain.product;

import com.clickstechnology.Brillo.Mall.application.api.contracts.ProductService;
import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.product.AddProductRequest;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public void ensureProductNameDoesNotExist(String businessId, String name) {
        if (productRepository.existsByBusinessIdAndNameIgnoreCase(businessId, name)) {
            throw new BusinessException("Product with name '%s' already exists for this business."
                    .formatted(name));
        }
    }

    @Override
    public void ensureProductSkuDoesNotExist(String businessId, String sku) {
        if (productRepository.existsByBusinessIdAndSkuIgnoreCase(businessId, sku)) {
            throw new BusinessException("Product with SKU '%s' already exists for this business."
                    .formatted(sku));
        }
    }

    @Override
    public ProductDto createProduct(String businessId, AddProductRequest request) {
        Product product = Product.builder()
                .businessId(businessId)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .discountedPrice(request.getDiscountedPrice())
                .sku(request.getSku())
                .quantity(request.getQuantity())
                .status(EntityStatus.ACTIVE)
                .build();
        
        product = productRepository.save(product);
        return product.dto();
    }
}