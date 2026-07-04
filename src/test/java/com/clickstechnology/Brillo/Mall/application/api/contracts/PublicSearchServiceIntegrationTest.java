package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.RegisterRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.AddBusinessServiceRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.OnboardBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.product.AddProductRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.dto.search.PublicSearchResultDto;
import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;
import com.clickstechnology.Brillo.Mall.application.enums.PricingType;
import com.clickstechnology.Brillo.Mall.application.enums.PublicSearchResultType;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessServiceService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.ProductService;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PublicSearchServiceIntegrationTest {

    @Autowired
    private PublicSearchService publicSearchService;

    @Autowired
    private BusinessService businessService;

    @Autowired
    private ProductService productService;

    @Autowired
    private BusinessServiceService businessServiceService;

    @Autowired
    private UserService userService;

    @MockitoBean
    private CacheUtil cacheUtil;

    @PersistenceContext
    private EntityManager entityManager;

    private UserDto owner;
    private BusinessDto productBusiness;
    private BusinessDto serviceBusiness;

    @BeforeEach
    void setUp() {
        RegisterRequest registerRequest = new RegisterRequest(
                "Herbal Owner",
                "07035009999",
                "Password123",
                null
        );
        userService.registerNewUser(registerRequest, null, false);
        entityManager.flush();
        owner = userService.findByUsername("07035009999");

        OnboardBusinessRequest productBusinessRequest = new OnboardBusinessRequest();
        productBusinessRequest.setBusinessName("Herbal Store");
        businessService.createNew(productBusinessRequest, owner, "store.png", BusinessCategory.PRODUCTS);

        OnboardBusinessRequest serviceBusinessRequest = new OnboardBusinessRequest();
        serviceBusinessRequest.setBusinessName("Herbal Services");
        businessService.createNew(serviceBusinessRequest, owner, "services.png", BusinessCategory.SERVICES);

        List<BusinessDto> businesses = businessService.findAllByOwnerId(owner.getId());
        productBusiness = businesses.stream()
                .filter(business -> "Herbal Store".equals(business.getName()))
                .findFirst()
                .orElseThrow();
        serviceBusiness = businesses.stream()
                .filter(business -> "Herbal Services".equals(business.getName()))
                .findFirst()
                .orElseThrow();

        AddProductRequest productRequest = new AddProductRequest(
                "Herbal Soap",
                "PHARMACY",
                "Gentle herbal soap",
                BigDecimal.valueOf(2500),
                BigDecimal.valueOf(2000),
                "SOAP-001",
                true,
                40
        );
        productService.createProduct(productBusiness.getId(), productRequest, "soap.png");

        AddBusinessServiceRequest serviceRequest = new AddBusinessServiceRequest(
                serviceBusiness.getId(),
                "Herbal Consultation",
                "Personal wellness consultation",
                "PHARMACY",
                PricingType.FIXED,
                BigDecimal.valueOf(5000),
                60,
                true,
                true
        );
        businessServiceService.addService(serviceRequest, owner);
    }

    @Test
    void search_shouldReturnMixedResultsAcrossAllPublicDomains() {
        PaginatedResponse<PublicSearchResultDto> response = publicSearchService.search("herbal", 1, 20);

        assertThat(response.getTotal()).isEqualTo(4);
        assertThat(response.getItems())
                .extracting(PublicSearchResultDto::getType)
                .containsExactlyInAnyOrder(
                        PublicSearchResultType.PRODUCT,
                        PublicSearchResultType.BUSINESS,
                        PublicSearchResultType.BUSINESS,
                        PublicSearchResultType.SERVICE);

        PublicSearchResultDto productResult = response.getItems().stream()
                .filter(item -> item.getType() == PublicSearchResultType.PRODUCT)
                .findFirst()
                .orElseThrow();
        assertThat(productResult.getName()).isEqualTo("Herbal Soap");
        assertThat(productResult.getCategory()).isEqualTo("Pharmacy");
        assertThat(productResult.getBusinessName()).isEqualTo("Herbal Store");

        PublicSearchResultDto serviceResult = response.getItems().stream()
                .filter(item -> item.getType() == PublicSearchResultType.SERVICE)
                .findFirst()
                .orElseThrow();
        assertThat(serviceResult.getName()).isEqualTo("Herbal Consultation");
        assertThat(serviceResult.getCategory()).isEqualTo("Pharmacy");
        assertThat(serviceResult.getBusinessName()).isEqualTo("Herbal Services");
    }

    @Test
    void search_shouldReturnEmptyPage_whenNothingMatches() {
        PaginatedResponse<PublicSearchResultDto> response = publicSearchService.search("no-such-term", 1, 20);

        assertThat(response.getTotal()).isZero();
        assertThat(response.getItems()).isEmpty();
        assertThat(response.getPage()).isEqualTo(1);
    }
}
