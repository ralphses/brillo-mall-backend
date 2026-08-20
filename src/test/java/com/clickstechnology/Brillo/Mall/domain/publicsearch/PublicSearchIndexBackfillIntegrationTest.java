package com.clickstechnology.Brillo.Mall.domain.publicsearch;

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
import com.clickstechnology.Brillo.Mall.application.enums.PublicSearchResultType;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PublicSearchIndexBackfillIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService businessService;

    @Autowired
    private ProductService productService;

    @Autowired
    private BusinessServiceService businessServiceService;

    @Autowired
    private PublicSearchIndexRepository publicSearchIndexRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
                "Backfill Owner",
                "07035001111",
                "Password123",
                null
        );
        userService.registerNewUser(registerRequest, null, false);
        entityManager.flush();
        owner = userService.findByUsername("07035001111");

        OnboardBusinessRequest productBusinessRequest = new OnboardBusinessRequest();
        productBusinessRequest.setBusinessName("Backfill Store");
        businessService.createNew(productBusinessRequest, owner, "store.png", BusinessCategory.PRODUCTS);

        OnboardBusinessRequest serviceBusinessRequest = new OnboardBusinessRequest();
        serviceBusinessRequest.setBusinessName("Backfill Services");
        businessService.createNew(serviceBusinessRequest, owner, "services.png", BusinessCategory.SERVICES);

        List<BusinessDto> businesses = businessService.findAllByOwnerId(owner.getId());
        productBusiness = businesses.stream()
                .filter(business -> "Backfill Store".equals(business.getName()))
                .findFirst()
                .orElseThrow();
        serviceBusiness = businesses.stream()
                .filter(business -> "Backfill Services".equals(business.getName()))
                .findFirst()
                .orElseThrow();

        AddProductRequest productRequest = new AddProductRequest(
                "Backfill Soap",
                "PHARMACY",
                "Soap for backfill test",
                BigDecimal.valueOf(2500),
                BigDecimal.valueOf(2000),
                "BFS-001",
                true,
                40
        );
        ProductDto product = productService.createProduct(productBusiness.getId(), productRequest, "soap.png");
        productId = product.getId();

        AddBusinessServiceRequest serviceRequest = new AddBusinessServiceRequest(
                serviceBusiness.getId(),
                "Backfill Consultation",
                "Consultation for backfill test",
                "PHARMACY",
                PricingType.FIXED,
                BigDecimal.valueOf(5000),
                60,
                true,
                true
        );
        BusinessServiceDto service = businessServiceService.addService(serviceRequest, owner);
        serviceId = service.getId();

        jdbcTemplate.update("delete from brillo_public_search_index");
    }

    @Test
    void backfillMigration_shouldPopulateVisiblePublicRows() throws Exception {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("db/migration/V12__public_search_index_backfill.sql"));
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }

        assertThat(publicSearchIndexRepository.count()).isEqualTo(4);
        assertThat(publicSearchIndexRepository.findAllByBusinessId(productBusiness.getId()))
                .extracting(PublicSearchIndex::getType)
                .contains(PublicSearchResultType.BUSINESS, PublicSearchResultType.PRODUCT);

        PublicSearchIndex productIndex = publicSearchIndexRepository.findByReferenceAndType(
                productId,
                PublicSearchResultType.PRODUCT).orElseThrow();
        assertThat(productIndex.getCategoryLabel()).isEqualTo("Pharmacy");
        assertThat(productIndex.getSearchText()).contains("backfill soap");

        assertThat(publicSearchIndexRepository.findByReferenceAndType(serviceId, PublicSearchResultType.SERVICE))
                .isPresent();
    }
}
