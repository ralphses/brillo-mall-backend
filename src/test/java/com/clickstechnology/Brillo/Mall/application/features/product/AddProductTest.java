package com.clickstechnology.Brillo.Mall.application.features.product;

import com.clickstechnology.Brillo.Mall.application.api.contracts.AuthenticationUtil;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.ProductService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.product.AddProductRequest;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.application.exception.UnauthorizedUserException;
import com.clickstechnology.Brillo.Mall.infrastructure.config.AppPropertiesConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddProductTest {

    @Mock
    private AppPropertiesConfig appPropertiesConfig;

    @Mock
    private ProductService productService;

    @Mock
    private BusinessService businessService;

    @Mock
    private AuthenticationUtil authenticationUtil;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AddProduct addProduct;

    private String businessId;
    private AddProductRequest request;
    private BusinessDto activeBusiness;
    private ProductDto expectedProductDto;

    @BeforeEach
    void setUp() {

        businessId = "biz-123";
        String userId = "user-456";

        request = new AddProductRequest(
                "Test Product",
                "Description",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(90),
                "SKU123",
                10
        );

        activeBusiness = new BusinessDto();
        activeBusiness.setId(businessId);
        activeBusiness.setStatus(EntityStatus.ACTIVE);
        activeBusiness.setOwnerId(userId);

        UserDto userDto = new UserDto();
        userDto.setId(userId);

        expectedProductDto = ProductDto.builder()
                .name("Test Product")
                .sku("SKU123")
                .build();

        when(authenticationUtil.getAuthenticatedUsername(httpServletRequest))
                .thenReturn("testuser");

        when(userService.findByUsername("testuser"))
                .thenReturn(userDto);
    }

    @Test
    void execute_Success_WithProvidedSku() {

        when(appPropertiesConfig.getDefaultProductImageUrl()).thenReturn("imageUrl");


        when(businessService.findByBusinessId(businessId))
                .thenReturn(activeBusiness);

        doNothing().when(productService)
                .ensureProductNameDoesNotExist(businessId, request.getName());

        doNothing().when(productService)
                .ensureProductSkuDoesNotExist(businessId, request.getSku());

        when(productService.createProduct(
                eq(businessId),
                eq(request),
                anyString()
        )).thenReturn(expectedProductDto);

        ProductDto result = addProduct.execute(businessId, request, httpServletRequest);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals("SKU123", result.getSku());

        verify(productService).createProduct(
                eq(businessId),
                eq(request),
                anyString()
        );
    }

    @Test
    void execute_Success_WithGeneratedSku() {
        when(businessService.findByBusinessId(businessId)).thenReturn(activeBusiness);
        when(appPropertiesConfig.getDefaultProductImageUrl()).thenReturn("imageUrl");

        request.setSku(null); // Force SKU generation
        doNothing().when(productService).ensureProductNameDoesNotExist(businessId, request.getName());

        // Mock that the generated SKU does not exist
        doNothing().when(productService).ensureProductSkuDoesNotExist(eq(businessId), anyString());
        when(productService.createProduct(eq(businessId), any(AddProductRequest.class), any())).thenReturn(expectedProductDto);

        ProductDto result = addProduct.execute(businessId, request, httpServletRequest);

        assertNotNull(result);
        verify(productService).ensureProductSkuDoesNotExist(eq(businessId), anyString());
        verify(productService).createProduct(eq(businessId), any(AddProductRequest.class), any());

        // Verify SKU was generated and set on the request
        assertNotNull(request.getSku());
        assertTrue(request.getSku().startsWith("PRD"));
    }

    @Test
    void execute_Failure_UnauthorizedOwner() {
        String authenticatedUserId = "user-456";
        doThrow(new UnauthorizedUserException())
                .when(businessService)
                .ensureBusinessBelongsToUser(businessId, authenticatedUserId);

        UnauthorizedUserException exception = assertThrows(UnauthorizedUserException.class, () -> addProduct.execute(businessId, request, httpServletRequest));
        assertEquals("Unauthorized User", exception.getMessage());

        verify(productService, never()).createProduct(anyString(), any(), anyString());
    }


    @Test
    void execute_ThrowsException_WhenBusinessNotActive() {
        activeBusiness.setStatus(EntityStatus.INACTIVE);

        when(businessService.findByBusinessId(businessId)).thenReturn(activeBusiness);

        BusinessException exception = assertThrows(BusinessException.class, () -> addProduct.execute(businessId, request, httpServletRequest));
        assertTrue(exception.getMessage().contains("Business is not active"));

        verify(productService, never()).createProduct(anyString(), any(), anyString());
    }

    @Test
    void execute_ThrowsException_WhenProductNameExists() {
        when(businessService.findByBusinessId(businessId)).thenReturn(activeBusiness);

        doThrow(new BusinessException("Product name exists")).when(productService).ensureProductNameDoesNotExist(businessId, request.getName());

        assertThrows(BusinessException.class, () -> addProduct.execute(businessId, request, httpServletRequest));

        verify(productService, never()).createProduct(anyString(), any(), anyString());
    }

    @Test
    void execute_ThrowsException_WhenProvidedSkuExists() {

        when(businessService.findByBusinessId(businessId)).thenReturn(activeBusiness);

        doNothing().when(productService).ensureProductNameDoesNotExist(businessId, request.getName());
        doThrow(new BusinessException("SKU exists")).when(productService).ensureProductSkuDoesNotExist(businessId, request.getSku());

        assertThrows(BusinessException.class, () -> addProduct.execute(businessId, request, httpServletRequest));

        verify(productService, never()).createProduct(anyString(), any(), anyString());
    }

    @Test
    void execute_ThrowsException_WhenGeneratedSkuCollidesMaxRetries() {
        when(businessService.findByBusinessId(businessId)).thenReturn(activeBusiness);

        request.setSku(null); // Force SKU generation
        doNothing().when(productService).ensureProductNameDoesNotExist(businessId, request.getName());

        // Mock SKU existence check to always throw exception (simulate collision)
        doThrow(new BusinessException("SKU exists")).when(productService).ensureProductSkuDoesNotExist(eq(businessId), anyString());

        BusinessException exception = assertThrows(BusinessException.class, () -> addProduct.execute(businessId, request, httpServletRequest));
        assertTrue(exception.getMessage().contains("Unable to generate a unique product SKU"));

        // Verify it retried exactly 3 times
        verify(productService, times(3)).ensureProductSkuDoesNotExist(eq(businessId), anyString());
        verify(productService, never()).createProduct(anyString(), any(), anyString());
    }
}