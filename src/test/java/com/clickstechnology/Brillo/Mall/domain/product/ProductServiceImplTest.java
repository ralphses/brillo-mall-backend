package com.clickstechnology.Brillo.Mall.domain.product;

import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.product.AddProductRequest;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    private String businessId;
    private AddProductRequest addProductRequest;

    @BeforeEach
    void setUp() {
        businessId = "biz-123";
        addProductRequest = new AddProductRequest(
                "Test Product",
                "Test Description",
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(90.00),
                "SKU-123",
                50
        );
    }

    @Test
    void ensureProductNameDoesNotExist_Success() {
        when(productRepository.existsByBusinessIdAndNameIgnoreCase(businessId, "Test Product"))
                .thenReturn(false);

        assertDoesNotThrow(() -> productService.ensureProductNameDoesNotExist(businessId, "Test Product"));
        verify(productRepository).existsByBusinessIdAndNameIgnoreCase(businessId, "Test Product");
    }

    @Test
    void ensureProductNameDoesNotExist_ThrowsException_WhenNameExists() {
        when(productRepository.existsByBusinessIdAndNameIgnoreCase(businessId, "Test Product"))
                .thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, 
                () -> productService.ensureProductNameDoesNotExist(businessId, "Test Product"));
        
        assertTrue(exception.getMessage().contains("already exists"));
        verify(productRepository).existsByBusinessIdAndNameIgnoreCase(businessId, "Test Product");
    }

    @Test
    void ensureProductSkuDoesNotExist_Success() {
        when(productRepository.existsByBusinessIdAndSkuIgnoreCase(businessId, "SKU-123"))
                .thenReturn(false);

        assertDoesNotThrow(() -> productService.ensureProductSkuDoesNotExist(businessId, "SKU-123"));
        verify(productRepository).existsByBusinessIdAndSkuIgnoreCase(businessId, "SKU-123");
    }

    @Test
    void ensureProductSkuDoesNotExist_ThrowsException_WhenSkuExists() {
        when(productRepository.existsByBusinessIdAndSkuIgnoreCase(businessId, "SKU-123"))
                .thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, 
                () -> productService.ensureProductSkuDoesNotExist(businessId, "SKU-123"));
        
        assertTrue(exception.getMessage().contains("already exists"));
        verify(productRepository).existsByBusinessIdAndSkuIgnoreCase(businessId, "SKU-123");
    }

    @Test
    void createProduct_Success() {
        Product savedProduct = Product.builder()
                .businessId(businessId)
                .name(addProductRequest.getName())
                .description(addProductRequest.getDescription())
                .price(addProductRequest.getPrice())
                .discountedPrice(addProductRequest.getDiscountedPrice())
                .sku(addProductRequest.getSku())
                .quantity(addProductRequest.getQuantity())
                .status(EntityStatus.ACTIVE)
                .build();
        savedProduct.setReference("prod-ref-123");

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductDto result = productService.createProduct(businessId, addProductRequest);

        assertNotNull(result);
        assertEquals("prod-ref-123", result.getId());
        assertEquals(businessId, result.getBusinessId());
        assertEquals("Test Product", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals(BigDecimal.valueOf(100.00), result.getPrice());
        assertEquals(BigDecimal.valueOf(90.00), result.getDiscountedPrice());
        assertEquals("SKU-123", result.getSku());
        assertEquals(50, result.getQuantity());
        assertEquals(EntityStatus.ACTIVE, result.getStatus());

        verify(productRepository).save(productCaptor.capture());
        Product capturedProduct = productCaptor.getValue();
        
        assertEquals(businessId, capturedProduct.getBusinessId());
        assertEquals("Test Product", capturedProduct.getName());
        assertEquals(EntityStatus.ACTIVE, capturedProduct.getStatus());
    }
}