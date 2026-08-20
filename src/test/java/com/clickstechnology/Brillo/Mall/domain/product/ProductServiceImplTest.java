package com.clickstechnology.Brillo.Mall.domain.product;

import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.product.AddProductRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.product.UpdateProductRequest;
import com.clickstechnology.Brillo.Mall.application.api.contracts.CategoryCatalogService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.PublicSearchIndexSync;
import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    public static final String DEFAULT_PRODUCT_IMG_URL = "http://image.com";

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryCatalogService categoryCatalogService;

    @Mock
    private PublicSearchIndexSync publicSearchIndexSync;

    @InjectMocks
    private ProductServiceImpl productService;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    @Captor
    private ArgumentCaptor<List<Product>> productListCaptor;

    private String businessId;
    private AddProductRequest addProductRequest;

    @BeforeEach
    void setUp() {
        businessId = "biz-123";
        addProductRequest = new AddProductRequest(
                "Test Product",
                "PHARMACY",
                "Test Description",
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(90.00),
                "SKU-123",
                true,
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
                .category("PHARMACY")
                .description(addProductRequest.getDescription())
                .price(addProductRequest.getPrice())
                .discountedPrice(addProductRequest.getDiscountedPrice())
                .sku(addProductRequest.getSku())
                .flashSale(true)
                .quantity(addProductRequest.getQuantity())
                .status(EntityStatus.ACTIVE)
                .build();
        savedProduct.setReference("prod-ref-123");

        when(categoryCatalogService.resolveCategory(BusinessCategory.PRODUCTS, addProductRequest.getCategory()))
                .thenReturn("PHARMACY");
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductDto result = productService.createProduct(businessId, addProductRequest, DEFAULT_PRODUCT_IMG_URL);

        assertNotNull(result);
        assertEquals("prod-ref-123", result.getId());
        assertEquals(businessId, result.getBusinessId());
        assertEquals("Test Product", result.getName());
        assertEquals("PHARMACY", result.getCategory());
        assertEquals("Test Description", result.getDescription());
        assertEquals(BigDecimal.valueOf(100.00), result.getPrice());
        assertEquals(BigDecimal.valueOf(90.00), result.getDiscountedPrice());
        assertEquals("SKU-123", result.getSku());
        assertTrue(result.isFlashSale());
        assertEquals(50, result.getQuantity());
        assertEquals(EntityStatus.ACTIVE, result.getStatus());

        verify(productRepository).save(productCaptor.capture());
        Product capturedProduct = productCaptor.getValue();
        
        assertEquals(businessId, capturedProduct.getBusinessId());
        assertEquals("Test Product", capturedProduct.getName());
        assertEquals("PHARMACY", capturedProduct.getCategory());
        assertTrue(capturedProduct.isFlashSale());
        assertEquals(EntityStatus.ACTIVE, capturedProduct.getStatus());
        verify(publicSearchIndexSync).syncProduct(savedProduct);
    }

    @Mock
    private ProductSpecification productSpecification;

    @Test
    void getProducts_Success() {
        Specification<Product> mockSpec = (root, query, criteriaBuilder) -> null;

        when(productSpecification.getProducts(businessId, null, null, null, null, null))
                .thenReturn(mockSpec);

        Page<Product> productPage = new PageImpl<>(List.of(new Product()));

        when(productRepository.findAll(eq(mockSpec), any(Pageable.class)))
                .thenReturn(productPage);

        var result = productService.getProducts(businessId, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());

        verify(productSpecification).getProducts(businessId, null, null, null, null, null);

        verify(productRepository).findAll(eq(mockSpec), any(Pageable.class));
    }

    @Test
    void getProducts_WithFilters_Success() {
        Specification<Product> mockSpec = (root, query, criteriaBuilder) -> null;

        when(productSpecification.getProducts(
                businessId,
                "shirt",
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(100),
                true,
                EntityStatus.ACTIVE))
                .thenReturn(mockSpec);

        Page<Product> productPage = new PageImpl<>(List.of(new Product()));
        when(productRepository.findAll(eq(mockSpec), any(Pageable.class))).thenReturn(productPage);

        var result = productService.getProducts(
                businessId,
                1,
                10,
                "shirt",
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(100),
                true,
                EntityStatus.ACTIVE);

        assertNotNull(result);
        verify(productSpecification).getProducts(
                businessId,
                "shirt",
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(100),
                true,
                EntityStatus.ACTIVE);
    }

    @Test
    void getProducts_WithPublicFlags_Success() {
        Specification<Product> mockSpec = (root, query, criteriaBuilder) -> null;

        when(productSpecification.getProducts(
                businessId,
                "shirt",
                "PHARMACY",
                true,
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(100),
                true,
                EntityStatus.ACTIVE))
                .thenReturn(mockSpec);

        Page<Product> productPage = new PageImpl<>(List.of(new Product()));
        when(productRepository.findAll(eq(mockSpec), any(Pageable.class))).thenReturn(productPage);

        var result = productService.getProducts(
                businessId,
                1,
                10,
                "shirt",
                "PHARMACY",
                true,
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(100),
                true,
                EntityStatus.ACTIVE);

        assertNotNull(result);
        verify(productSpecification).getProducts(
                businessId,
                "shirt",
                "PHARMACY",
                true,
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(100),
                true,
                EntityStatus.ACTIVE);
    }

    @Test
    void findProductByProductId_Success() {
        // Given
        Product product = new Product();
        product.setReference("prod-123");
        when(productRepository.findByReference("prod-123")).thenReturn(Optional.of(product));

        // When
        ProductDto result = productService.findProductByProductId("prod-123");

        // Then
        assertNotNull(result);
        assertEquals("prod-123", result.getId());
    }

    @Test
    void findProductByProductId_ThrowsException_WhenNotFound() {
        // Given
        when(productRepository.findByReference("prod-123")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> productService.findProductByProductId("prod-123"));
    }

    @Test
    void findProductBySku_Success() {
        // Given
        Product product = new Product();
        product.setSku("SKU-123");
        when(productRepository.findBySku("SKU-123")).thenReturn(Optional.of(product));

        // When
        ProductDto result = productService.findProductBySku("SKU-123");

        // Then
        assertNotNull(result);
        assertEquals("SKU-123", result.getSku());
    }

    @Test
    void findProductBySku_ThrowsException_WhenNotFound() {
        // Given
        when(productRepository.findBySku("SKU-123")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> productService.findProductBySku("SKU-123"));
    }

    @Test
    void deleteProduct_Success() {
        // Given
        Product product = new Product();
        product.setStatus(EntityStatus.ACTIVE);
        when(productRepository.findByReference("prod-123")).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        productService.deleteProduct("prod-123");

        // Then
        verify(productRepository).save(productCaptor.capture());
        assertEquals(EntityStatus.DELETED, productCaptor.getValue().getStatus());
        verify(publicSearchIndexSync).syncProduct(productCaptor.getValue());
    }

    @Test
    void updateProduct_Success() {
        // Given
        Product product = new Product();
        product.setName("Old Name");
        product.setFlashSale(false);
        when(productRepository.findByReference("prod-123")).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("New Name");
        request.setFlashSale(true);

        // When
        ProductDto result = productService.updateProduct("prod-123", request);

        // Then
        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertTrue(result.isFlashSale());
        verify(publicSearchIndexSync).syncProduct(product);
    }

    @Test
    void findProductsByIds_Success() {
        // Given
        List<String> productIds = List.of("prod-123");
        when(productRepository.findAllByReferenceIn(productIds)).thenReturn(List.of(new Product()));

        // When
        productService.findProductsByIds(productIds);

        // Then
        verify(productRepository).findAllByReferenceIn(productIds);
    }

    @Test
    void findAllByProductIds_Success() {
        // Given
        when(productRepository.findAllByReferenceIn(List.of("prod-123"))).thenReturn(List.of(new Product()));

        // When
        var result = productService.findAllByProductIds(Set.of("prod-123"));

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void checkInStock_Success() {
        // Given
        Product product = new Product();
        product.setReference("prod-123");
        product.setQuantity(10);
        when(productRepository.findAllByReferenceIn(List.of("prod-123"))).thenReturn(List.of(product));

        // When & Then
        assertDoesNotThrow(() -> productService.checkInStock(Map.of("prod-123", 5)));
    }

    @Test
    void checkInStock_ThrowsException_WhenInsufficientStock() {
        // Given
        Product product = new Product();
        product.setReference("prod-123");
        product.setQuantity(10);
        product.setName("Test Product");
        when(productRepository.findAllByReferenceIn(List.of("prod-123"))).thenReturn(List.of(product));

        // When & Then
        assertThrows(BusinessException.class, () -> productService.checkInStock(Map.of("prod-123", 15)));
    }

    @Test
    void reduceStock_Success() {
        // Given
        Product product = new Product();
        product.setReference("prod-123");
        product.setQuantity(10);
        when(productRepository.findAllByReferenceIn(List.of("prod-123"))).thenReturn(List.of(product));

        // When
        productService.reduceStock(Map.of("prod-123", 5));

        // Then
        verify(productRepository).saveAll(productListCaptor.capture());
        List<Product> capturedProducts = productListCaptor.getValue();
        assertEquals(1, capturedProducts.size());
        assertEquals(5, capturedProducts.get(0).getQuantity());
    }

    @Test
    void reduceStock_ThrowsException_WhenProductNotFound() {
        // Given
        when(productRepository.findAllByReferenceIn(List.of("prod-123"))).thenReturn(List.of());

        // When & Then
        assertThrows(BusinessException.class, () -> productService.reduceStock(Map.of("prod-123", 5)));
    }

    @Test
    void reduceStock_ThrowsException_WhenInsufficientStock() {
        // Given
        Product product = new Product();
        product.setReference("prod-123");
        product.setQuantity(10);
        when(productRepository.findAllByReferenceIn(List.of("prod-123"))).thenReturn(List.of(product));

        // When & Then
        assertThrows(BusinessException.class, () -> productService.reduceStock(Map.of("prod-123", 15)));
    }
}
