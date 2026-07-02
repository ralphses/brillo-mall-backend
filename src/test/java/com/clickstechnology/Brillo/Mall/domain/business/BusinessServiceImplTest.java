package com.clickstechnology.Brillo.Mall.domain.business;

import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.OnboardBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.UpdateBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.application.exception.UnauthorizedUserException;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheNames;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessServiceImplTest {

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private CacheUtil cacheUtil;

    @InjectMocks
    private BusinessServiceImpl businessService;

    private Business business;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setId("user1");

        business = new Business();
        business.setReference("biz1");
        business.setName("Test Business");
        business.setOwnerId(userDto.getId());
        business.setIsActive(true);
    }

    @Nested
    @DisplayName("ensureBusinessNameDoesNotExist tests")
    class EnsureBusinessNameDoesNotExistTests {

        @Test
        @DisplayName("Should throw BusinessException when name exists")
        void shouldThrowExceptionWhenNameExists() {
            // Given
            when(businessRepository.existsByName("Test Business")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> businessService.ensureBusinessNameDoesNotExist("Test Business"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Business name already exists");
        }

        @Test
        @DisplayName("Should not throw exception when name does not exist")
        void shouldNotThrowExceptionWhenNameDoesNotExist() {
            // Given
            when(businessRepository.existsByName("New Business")).thenReturn(false);

            // When & Then
            businessService.ensureBusinessNameDoesNotExist("New Business");
            // No exception thrown
        }
    }

    @Nested
    @DisplayName("ensureBusinessBelongsToUser tests")
    class EnsureBusinessBelongsToUserTests {

        @Test
        @DisplayName("Should not throw exception when business belongs to user")
        void shouldNotThrowExceptionWhenBusinessBelongsToUser() {
            // Given
            when(cacheUtil.get(anyString(), eq(Business.class))).thenReturn(business);

            // When & Then
            businessService.ensureBusinessBelongsToUser("biz1", "user1");
            // No exception thrown
        }

        @Test
        @DisplayName("Should throw UnauthorizedUserException when business does not belong to user")
        void shouldThrowExceptionWhenBusinessDoesNotBelongToUser() {
            // Given
            when(cacheUtil.get(anyString(), eq(Business.class))).thenReturn(business);

            // When & Then
            assertThatThrownBy(() -> businessService.ensureBusinessBelongsToUser("biz1", "user2"))
                    .isInstanceOf(UnauthorizedUserException.class);
        }
    }

    @Test
    @DisplayName("createNew should save a new business")
    void createNew_shouldSaveNewBusiness() {
        // Given
        OnboardBusinessRequest request = OnboardBusinessRequest.builder()
                .businessName("New Biz")
                .build();

        // When
        businessService.createNew(request, userDto, "logo.url", BusinessCategory.PRODUCTS);

        // Then
        ArgumentCaptor<Business> businessCaptor = ArgumentCaptor.forClass(Business.class);
        verify(businessRepository).save(businessCaptor.capture());
        Business savedBusiness = businessCaptor.getValue();

        assertThat(savedBusiness.getName()).isEqualTo("New Biz");
        assertThat(savedBusiness.getOwnerId()).isEqualTo("user1");
        assertThat(savedBusiness.getSlug()).isEqualTo("new-biz");
    }

    @Nested
    @DisplayName("findByBusinessId tests")
    class FindByBusinessIdTests {

        @Test
        @DisplayName("Should return business from cache if present")
        void shouldReturnBusinessFromCache() {
            // Given
            when(cacheUtil.get(anyString(), eq(Business.class))).thenReturn(business);

            // When
            BusinessDto result = businessService.findByBusinessId("biz1");

            // Then
            assertThat(result.getId()).isEqualTo("biz1");
            verify(businessRepository, never()).findByReference(anyString());
        }

        @Test
        @DisplayName("Should return business from repository if not in cache")
        void shouldReturnBusinessFromRepository() {
            // Given
            when(cacheUtil.get(anyString(), eq(Business.class))).thenReturn(null);
            when(businessRepository.findByReference("biz1")).thenReturn(Optional.of(business));

            // When
            BusinessDto result = businessService.findByBusinessId("biz1");

            // Then
            assertThat(result.getId()).isEqualTo("biz1");
            verify(cacheUtil).set(anyString(), eq(business), any());
        }
    }

    @Test
    @DisplayName("updateBusiness should update business details")
    void updateBusiness_shouldUpdateDetails() {
        // Given
        UpdateBusinessRequest request = new UpdateBusinessRequest();
        request.setBusinessEmail("new@email.com");
        when(cacheUtil.get(anyString(), eq(Business.class))).thenReturn(business);

        // When
        businessService.updateBusiness("biz1", request);

        // Then
        verify(businessRepository).save(business);
        assertThat(business.getEmail()).isEqualTo("new@email.com");
    }

    @Test
    @DisplayName("findAllByOwnerId should return paginated businesses")
    void findAllByOwnerId_shouldReturnPaginatedBusinesses() {
        // Given
        Page<Business> page = new PageImpl<>(List.of(business));
        when(businessRepository.findAllByOwnerId(anyString(), any(Pageable.class))).thenReturn(page);

        // When
        PaginatedResponse<BusinessDto> response = businessService.findAllByOwnerId("user1", 1, 10);

        // Then
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getId()).isEqualTo("biz1");
        assertThat(response.getTotal()).isEqualTo(1);
    }

    @Nested
    @DisplayName("validateBusinessIsActive tests")
    class ValidateBusinessIsActiveTests {

        @Test
        @DisplayName("Should not throw exception for active businesses")
        void shouldNotThrowExceptionForActiveBusinesses() {
            // Given
            when(businessRepository.findAllByReferenceIn(anySet())).thenReturn(List.of(business));

            // When & Then
            businessService.validateBusinessIsActive(Set.of("biz1"));
            // No exception
        }

        @Test
        @DisplayName("Should throw BusinessException for inactive businesses")
        void shouldThrowExceptionForInactiveBusinesses() {
            // Given
            business.setIsActive(false);
            when(businessRepository.findAllByReferenceIn(anySet())).thenReturn(List.of(business));

            // When & Then
            assertThatThrownBy(() -> businessService.validateBusinessIsActive(Set.of("biz1")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("This business is not active");
        }
    }

    @Nested
    @DisplayName("findByBusinessSlug tests")
    class FindByBusinessSlugTests {

        @Test
        @DisplayName("Should return business from cache when slug exists")
        void findByBusinessSlug_shouldReturnFromCache_whenExists() {
            // Given
            String slug = "test-business";
            when(cacheUtil.get(CacheNames.BUSINESS_SLUG + slug, Business.class)).thenReturn(business);

            // When
            BusinessDto result = businessService.findByBusinessSlug(slug);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Test Business");
            verify(businessRepository, never()).findBySlug(anyString());
        }

        @Test
        @DisplayName("Should return business from repository when slug not in cache")
        void findByBusinessSlug_shouldReturnFromRepository_whenNotInCache() {
            // Given
            String slug = "test-business";
            when(cacheUtil.get(CacheNames.BUSINESS_SLUG + slug, Business.class)).thenReturn(null);
            when(businessRepository.findBySlug(slug)).thenReturn(Optional.of(business));

            // When
            BusinessDto result = businessService.findByBusinessSlug(slug);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Test Business");
            verify(businessRepository).findBySlug(slug);
            verify(cacheUtil).set(anyString(), any(Business.class), any());
        }

        @Test
        @DisplayName("Should throw BusinessException when slug does not exist")
        void findByBusinessSlug_shouldThrowException_whenNotFound() {
            // Given
            String slug = "non-existent-slug";
            when(cacheUtil.get(CacheNames.BUSINESS_SLUG + slug, Business.class)).thenReturn(null);
            when(businessRepository.findBySlug(slug)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> businessService.findByBusinessSlug(slug))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Business not found");
        }
    }

    @Nested
    @DisplayName("existsByBusinessId tests")
    class ExistsByBusinessIdTests {

        @Test
        @DisplayName("Should return true when business exists")
        void existsByBusinessId_shouldReturnTrue_whenExists() {
            // Given
            when(businessRepository.existsByReference("biz1")).thenReturn(true);

            // When
            boolean exists = businessService.existsByBusinessId("biz1");

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false when business does not exist")
        void existsByBusinessId_shouldReturnFalse_whenNotExists() {
            // Given
            when(businessRepository.existsByReference("biz2")).thenReturn(false);

            // When
            boolean exists = businessService.existsByBusinessId("biz2");

            // Then
            assertThat(exists).isFalse();
        }
    }

    @Test
    @DisplayName("addLogo should update the business logo URL")
    void addLogo_shouldUpdateLogoUrl() {
        // Given
        String logoUrl = "http://example.com/logo.png";
        when(cacheUtil.get(anyString(), eq(Business.class))).thenReturn(business);

        // When
        businessService.addLogo("biz1", logoUrl);

        // Then
        verify(businessRepository).save(business);
        assertThat(business.getLogoUrl()).isEqualTo(logoUrl);
    }

    @Test
    @DisplayName("findBusinessCustomers should return a set of customer IDs")
    void findBusinessCustomers_shouldReturnCustomerIds() {
        // Given
        business.setCustomers(Set.of("cust1", "cust2"));
        when(cacheUtil.get(anyString(), eq(Business.class))).thenReturn(business);

        // When
        Set<String> customers = businessService.findBusinessCustomers("biz1");

        // Then
        assertThat(customers).containsExactlyInAnyOrder("cust1", "cust2");
    }

    @Test
    @DisplayName("findAllByOwnerId (non-paginated) should return a list of businesses")
    void findAllByOwnerId_nonPaginated_shouldReturnListOfBusinesses() {
        // Given
        when(businessRepository.findAllByOwnerId("user1")).thenReturn(List.of(business));

        // When
        List<BusinessDto> result = businessService.findAllByOwnerId("user1");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("biz1");
    }

    @Test
    @DisplayName("findAllByBusinessIds should return a list of businesses")
    void findAllByBusinessIds_shouldReturnListOfBusinesses() {
        // Given
        when(businessRepository.findAllByReferenceIn(Set.of("biz1"))).thenReturn(List.of(business));

        // When
        List<BusinessDto> result = businessService.findAllByBusinessIds(Set.of("biz1"));

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("biz1");
    }

    @Test
    @DisplayName("addCustomer should add a customer to businesses")
    void addCustomer_shouldAddCustomerToBusinesses() {
        // Given
        CustomerDto customerDto = new CustomerDto();
        customerDto.setId("cust1");
        when(businessRepository.findAllByReferenceIn(anySet())).thenReturn(List.of(business));

        // When
        businessService.addCustomer(customerDto, Set.of("biz1"));

        // Then
        verify(businessRepository).saveAll(List.of(business));
        assertThat(business.getCustomers()).contains("cust1");
    }
}
