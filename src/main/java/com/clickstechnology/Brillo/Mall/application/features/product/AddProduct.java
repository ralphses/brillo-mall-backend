package com.clickstechnology.Brillo.Mall.application.features.product;

import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.ProductService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.TenantContextResolver;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.product.AddProductRequest;
import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.infrastructure.config.AppPropertiesConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.clickstechnology.Brillo.Mall.application.utils.AppConstants.SECURE_RANDOM;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddProduct {

    private final ProductService productService;
    private final BusinessService businessService;
    private final TenantContextResolver tenantContextResolver;
    private final AppPropertiesConfig appPropertiesConfig;

    /**
     * Executes the process of adding a new product to a specific business.
     * <p>
     * This method performs several validation steps before creating the product:
     * 1. Verifies that the business exists and is currently active.
     * 2. Ensures that no other product with the same name exists for this business.
     * 3. Validates the provided SKU for uniqueness, or generates a unique SKU if none is provided.
     * </p>
     * <p>
     * The operation is transactional, ensuring that any failure during the process
     * will roll back any partial state changes.
     * </p>
     *
     * @param businessId         The unique identifier of the business adding the product.
     * @param request            The request payload containing the product details (name, price, SKU, etc.).
     * @param httpServletRequest The HTTP request object, used to extract the authenticated username.
     * @return A {@link ProductDto} representing the successfully created product.
     * @throws BusinessException if the business is inactive, the product name already exists,
     *                           the provided SKU already exists, or a unique SKU cannot be generated.
     */
    @Transactional
    public ProductDto execute(String businessId, AddProductRequest request, HttpServletRequest httpServletRequest) {
        log.info(":::Attempting to add new product '{}' for businessId: {}", request.getName(), businessId);

        // 1. Ensure this business belongs to this user
        tenantContextResolver.ensureBusinessOwnership(httpServletRequest, businessId);

        // 2. Validate Business State
        BusinessDto business = businessService.findByBusinessId(businessId);
        if (business.getStatus() != EntityStatus.ACTIVE) {
            log.warn(":::Failed to add product: Business {} is not active.", businessId);
            throw new BusinessException("Business is not active. Kindly verify or contact admin for support.");
        }

        // 3. Validate Business State
        if (business.getCategory() != BusinessCategory.PRODUCTS) {
            log.warn(":::Failed to add product: Category is not PRODUCTS.");
            throw new BusinessException("This business can only accept PRODUCTS. Kindly verify or contact admin.");
        }

        // 4. Ensure product name uniqueness
        productService.ensureProductNameDoesNotExist(businessId, request.getName());

        // 5. Handle SKU logic (Generate if missing, validate if present)
        String sku = request.getSku();
        if (sku == null || sku.isBlank()) {
            sku = generateUniqueSku(businessId);
            request.setSku(sku);
            log.debug(":::Generated unique SKU {} for new product", sku);
        } else {
            productService.ensureProductSkuDoesNotExist(businessId, sku);
        }

        // 6. Create product and return dto
        ProductDto createdProduct = productService.createProduct(businessId, request, appPropertiesConfig.getDefaultProductImageUrl());
        log.info(":::Successfully added product '{}' with SKU: {} for businessId: {}", createdProduct.getName(), createdProduct.getSku(), businessId);

        return createdProduct;
    }

    private String generateUniqueSku(String businessId) {
        int maxRetries = 3;
        int attempts = 0;

        while (attempts < maxRetries) {
            long randomNum = (long) (SECURE_RANDOM.nextDouble() * 1_000_000_000_000L);
            String generatedSku = "PRD" + String.format("%012d", randomNum);

            try {
                // Check if the generated SKU already exists
                productService.ensureProductSkuDoesNotExist(businessId, generatedSku);
                return generatedSku; // It's unique, return it
            } catch (BusinessException e) {
                attempts++;
                log.warn("SKU collision detected for generated SKU: {}. Retrying... (Attempt {}/{})", generatedSku, attempts, maxRetries);
            }
        }

        log.error(":::Failed to generate a unique SKU for businessId: {} after {} attempts.", businessId, maxRetries);
        throw new BusinessException("Unable to generate a unique product SKU at this time. Please try again.");
    }
}
