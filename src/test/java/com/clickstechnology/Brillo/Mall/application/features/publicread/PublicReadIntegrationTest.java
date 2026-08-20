package com.clickstechnology.Brillo.Mall.application.features.publicread;

import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessServiceService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.ProductService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.RegisterRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.AddBusinessServiceRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.OnboardBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.product.AddProductRequest;
import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;
import com.clickstechnology.Brillo.Mall.application.enums.PricingType;
import com.clickstechnology.Brillo.Mall.application.exception.ResourceNotFoundException;
import com.clickstechnology.Brillo.Mall.domain.business.Business;
import com.clickstechnology.Brillo.Mall.domain.business.BusinessRepository;
import com.clickstechnology.Brillo.Mall.domain.business_service.BusinessServiceRepository;
import com.clickstechnology.Brillo.Mall.domain.product.Product;
import com.clickstechnology.Brillo.Mall.domain.product.ProductRepository;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class PublicReadIntegrationTest {

    @Autowired
    private PublicRead publicRead;

    @Autowired
    private UserService userService;

    @Autowired
    private com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService businessService;

    @Autowired
    private ProductService productService;

    @Autowired
    private BusinessServiceService businessServiceService;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BusinessServiceRepository businessServiceRepository;

    @MockitoBean
    private CacheUtil cacheUtil;

    @PersistenceContext
    private EntityManager entityManager;

    private UserDto owner;
    private BusinessDto productBusiness;
    private BusinessDto serviceBusiness;

    private String productId;
    private String serviceId;

    @BeforeEach
    void setUp() {
        RegisterRequest registerRequest = new RegisterRequest(
                "Public Owner",
                "07035000000",
                "Password123",
                null
        );
        userService.registerNewUser(registerRequest, null, false);
        entityManager.flush();
        owner = userService.findByUsername("07035000000");

        OnboardBusinessRequest productBusinessRequest = new OnboardBusinessRequest();
        productBusinessRequest.setBusinessName("Public Store");
        businessService.createNew(productBusinessRequest, owner, "store.png", BusinessCategory.PRODUCTS);

        OnboardBusinessRequest serviceBusinessRequest = new OnboardBusinessRequest();
        serviceBusinessRequest.setBusinessName("Public Services");
        businessService.createNew(serviceBusinessRequest, owner, "services.png", BusinessCategory.SERVICES);

        List<BusinessDto> businesses = businessService.findAllByOwnerId(owner.getId());
        productBusiness = businesses.stream()
                .filter(business -> "Public Store".equals(business.getName()))
                .findFirst()
                .orElseThrow();
        serviceBusiness = businesses.stream()
                .filter(business -> "Public Services".equals(business.getName()))
                .findFirst()
                .orElseThrow();

        AddProductRequest productRequest = new AddProductRequest(
                "Public Soap",
                "PHARMACY",
                "Gentle herbal soap",
                BigDecimal.valueOf(2500),
                BigDecimal.valueOf(2000),
                "SOAP-100",
                true,
                40
        );
        ProductDto product = productService.createProduct(productBusiness.getId(), productRequest, "soap.png");
        productId = product.getId();

        AddBusinessServiceRequest serviceRequest = new AddBusinessServiceRequest(
                serviceBusiness.getId(),
                "Public Consultation",
                "Personal wellness consultation",
                "PHARMACY",
                PricingType.FIXED,
                BigDecimal.valueOf(5000),
                60,
                true,
                true
        );
        BusinessServiceDto service = businessServiceService.addService(serviceRequest, owner);
        serviceId = service.getId();
    }

    @Test
    void getProduct_shouldReturnVisibleProduct() {
        ProductDto product = publicRead.getProduct(productId);

        assertThat(product.getId()).isEqualTo(productId);
        assertThat(product.getBusinessId()).isEqualTo(productBusiness.getId());
        assertThat(product.getName()).isEqualTo("Public Soap");
    }

    @Test
    void getService_shouldReturnVisibleService() {
        BusinessServiceDto service = publicRead.getService(serviceId);

        assertThat(service.getId()).isEqualTo(serviceId);
        assertThat(service.getBusinessId()).isEqualTo(serviceBusiness.getId());
        assertThat(service.getSlug()).isEqualTo("public-consultation");
    }

    @Test
    void getBusiness_shouldReturnVisibleBusiness() {
        BusinessDto business = publicRead.getBusiness(productBusiness.getId());

        assertThat(business.getId()).isEqualTo(productBusiness.getId());
        assertThat(business.getName()).isEqualTo("Public Store");
    }

    @Test
    void getProduct_shouldReturnNotFound_whenProductDeleted() {
        productService.deleteProduct(productId);

        assertThatThrownBy(() -> publicRead.getProduct(productId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found");
    }

    @Test
    void getService_shouldReturnNotFound_whenServiceDeleted() {
        businessServiceService.deleteBusinessService(serviceId);

        assertThatThrownBy(() -> publicRead.getService(serviceId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Business service not found");
    }

    @Test
    void getBusiness_shouldReturnNotFound_whenBusinessHidden() {
        Business business = businessRepository.findByReference(productBusiness.getId()).orElseThrow();
        business.setStorefrontActive(false);
        businessRepository.save(business);
        entityManager.flush();

        assertThatThrownBy(() -> publicRead.getBusiness(productBusiness.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Business not found");
    }

    @Test
    void getProduct_shouldReturnNotFound_whenProductMissing() {
        assertThatThrownBy(() -> publicRead.getProduct("missing-product"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found");
    }

    @Test
    void getService_shouldReturnNotFound_whenServiceMissing() {
        assertThatThrownBy(() -> publicRead.getService("missing-service"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Business service not found");
    }

    @Test
    void getBusiness_shouldReturnNotFound_whenBusinessMissing() {
        assertThatThrownBy(() -> publicRead.getBusiness("missing-business"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Business not found");
    }
}
