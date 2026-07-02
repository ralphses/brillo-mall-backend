package com.clickstechnology.Brillo.Mall.domain.business;

import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.OnboardBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.UpdateBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;
import com.clickstechnology.Brillo.Mall.application.enums.WhatsappType;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.application.exception.UnauthorizedUserException;
import com.clickstechnology.Brillo.Mall.application.utils.AppUtils;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheNames;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;
    private final CacheUtil cacheUtil;

    @Override
    public void ensureBusinessNameDoesNotExist(String businessName) {
        if (businessRepository.existsByName(businessName)) {
            throw new BusinessException("Business name already exists");
        }

    }

    @Override
    public void ensureBusinessBelongsToUser(String businessId, String userId) {
        Business business = getBusinessByReference(businessId);
        if (!business.getOwnerId().equals(userId)) {
            throw new UnauthorizedUserException();
        }
    }

    @Override
    public void createNew(OnboardBusinessRequest request, UserDto user, String logoUrl, BusinessCategory businessCategory) {

        String slug = AppUtils.generateSlug(request.getBusinessName());

        Business business = Business.builder()
                .category(businessCategory)
                .name(request.getBusinessName())
                .slug(slug)
                .ownerId(user.getId())
                .logoUrl(logoUrl)
                .storefrontName(request.getBusinessName())
                .whatsappType(WhatsappType.SHARED)
                .build();
        businessRepository.save(business);
    }

    @Override
    public BusinessDto findByBusinessId(String businessId) {
        Business business = getBusinessByReference(businessId);
        return business.dto();
    }

    @Override
    public BusinessDto findByBusinessSlug(String businessSlug) {
        Business business = cacheUtil.get(CacheNames.BUSINESS_SLUG + businessSlug, Business.class);

        if (business == null) {
            business = businessRepository.findBySlug(businessSlug)
                    .orElseThrow(() -> new BusinessException("Business not found"));
            cacheUtil.set(CacheNames.BUSINESS_SLUG + businessSlug, business, Duration.ofMinutes(5));
        }

        return business.dto();
    }

    private Business getBusinessByReference(String businessId) {
        Business business = cacheUtil.get(CacheNames.BUSINESS_REFERENCE + businessId, Business.class);

        if (business == null) {
            business = businessRepository.findByReference(businessId)
                    .orElseThrow(() -> new BusinessException("Business not found"));
            cacheUtil.set(CacheNames.BUSINESS_REFERENCE + businessId, business, Duration.ofMinutes(5));
        }

        return business;
    }

    @Override
    public boolean existsByBusinessId(String businessId) {
        return businessRepository.existsByReference(businessId);
    }

    @Override
    public void addLogo(String businessId, String logoUrl) {
        Business business = getBusinessByReference(businessId);
        business.setLogoUrl(logoUrl);
        businessRepository.save(business);
    }

    @Override
    public void updateBusiness(String businessId, UpdateBusinessRequest request) {

        Business business = getBusinessByReference(businessId);

        if (request.getBusinessEmail() != null) {
            business.setEmail(request.getBusinessEmail());
        }

        if (request.getBusinessPhone() != null) {
            business.setPhoneNumber(request.getBusinessPhone());
        }

        if (request.getWhatsappNumber() != null) {
            business.setWhatsappNumber(request.getWhatsappNumber().replaceAll("[^\\d]", ""));
        }

        if (request.getWhatsappType() != null) {
            business.setWhatsappType(request.getWhatsappType());
        }

        if (request.getDescription() != null) {
            business.setDescription(request.getDescription());
        }

        if (request.getDisplayName() != null) {
            business.setStorefrontName(request.getDisplayName());
        }

        if (request.getBusinessAddress() != null) {
            var address = request.getBusinessAddress();

            if (address.getCity() != null) {
                business.setCity(address.getCity());
            }

            if (address.getState() != null) {
                business.setState(address.getState());
            }

            if (address.getStreetAddress() != null) {
                business.setAddress(address.getStreetAddress());
            }
        }

        businessRepository.save(business);
    }

    @Override
    public void activateStorefront(String businessId) {
        Business business = getBusinessByReference(businessId);
        business.setStorefrontActive(true);
        business.setSetupCompleted(true);
        businessRepository.save(business);
    }

    @Override
    public PaginatedResponse<BusinessDto> findAllByOwnerId(String userId, Integer page, Integer pageSize) {
        Pageable pageable = AppUtils.getPageable(page, pageSize);
        Page<Business> businessPage = businessRepository.findAllByOwnerId(userId, pageable);
        
        List<BusinessDto> items = businessPage.getContent().stream()
                .map(Business::dto)
                .toList();
        log.info(":::Total items: {}", items.size());
        log.info(":::Page items: {}", userId);

        return PaginatedResponse.<BusinessDto>builder()
                .page(page)
                .perPage(pageSize)
                .total((int) businessPage.getTotalElements())
                .totalPages(businessPage.getTotalPages())
                .hasNext(businessPage.hasNext())
                .hasPrevious(businessPage.hasPrevious())
                .items(items)
                .build();
    }

    @Override
    public void validateBusinessIsActive(Set<String> allProductOwners) {
        List<Business> businessList = businessRepository.findAllByReferenceIn(allProductOwners);
        for (Business business : businessList) {
            if (!business.getIsActive()) {
                throw new BusinessException("This business is not active");
            }
        }
    }

    @Override
    @Transactional
    public void addCustomer(CustomerDto customer, Set<String> businessIds) {
        List<Business> alBusinesses = businessRepository.findAllByReferenceIn(businessIds);
        for (Business business : alBusinesses) {
            business.addCustomer(customer.getId());
        }
        businessRepository.saveAll(alBusinesses);
    }

    @Override
    public Set<String> findBusinessCustomers(String businessId) {
        return getBusinessByReference(businessId).getCustomers();
    }

    @Override
    public List<BusinessDto> findAllByOwnerId(String userId) {
        return businessRepository.findAllByOwnerId(userId)
                .stream().map(Business::dto)
                .toList();
    }

    @Override
    public List<BusinessDto> findAllByBusinessIds(Set<String> businessIds) {
        return businessRepository.findAllByReferenceIn(businessIds)
                .stream().map(Business::dto)
                .toList();
    }

    @Override
    public Optional<BusinessDto> findByWhatsappNumber(String whatsappNumber) {
        return businessRepository.findByWhatsappNumber(whatsappNumber)
                .map(Business::dto);
    }

    @Override
    public List<BusinessDto> findWhatsappRouteCandidates() {
        return businessRepository.findTop10ByIsActiveTrueOrderByCreatedAtAsc()
                .stream()
                .filter(business -> Boolean.TRUE.equals(business.getIsActive()))
                .map(Business::dto)
                .toList();
    }
}
