package com.clickstechnology.Brillo.Mall.domain.product;

import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.ProductService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.RegisterRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.OnboardBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.product.AddProductRequest;
import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProductFlashSaleIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private BusinessService businessService;

    @Autowired
    private ProductService productService;

    @MockitoBean
    private CacheUtil cacheUtil;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void createProduct_shouldPersistFlashSaleFlag() {
        RegisterRequest registerRequest = new RegisterRequest(
                "Flash Owner",
                "07035008888",
                "Password123",
                null
        );
        userService.registerNewUser(registerRequest, null, false);
        entityManager.flush();

        UserDto owner = userService.findByUsername("07035008888");

        OnboardBusinessRequest onboardBusinessRequest = new OnboardBusinessRequest();
        onboardBusinessRequest.setBusinessName("Flash Store");
        businessService.createNew(onboardBusinessRequest, owner, "logo.png", BusinessCategory.PRODUCTS);
        BusinessDto business = businessService.findByBusinessSlug("flash-store");

        AddProductRequest request = new AddProductRequest(
                "Flash Product",
                "PHARMACY",
                "Sale item",
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(800),
                "FLASH-001",
                true,
                25
        );

        var createdProduct = productService.createProduct(business.getId(), request, "image.png");

        assertThat(createdProduct.isFlashSale()).isTrue();
        assertThat(productService.findProductByProductId(createdProduct.getId()).isFlashSale()).isTrue();
    }
}
