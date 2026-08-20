package com.clickstechnology.Brillo.Mall.domain.publicsearch;

import com.clickstechnology.Brillo.Mall.application.api.contracts.CategoryCatalogService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.PublicSearchIndexSync;
import com.clickstechnology.Brillo.Mall.application.dto.category.CategoryDto;
import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.enums.PublicSearchResultType;
import com.clickstechnology.Brillo.Mall.domain.business.Business;
import com.clickstechnology.Brillo.Mall.domain.business.BusinessRepository;
import com.clickstechnology.Brillo.Mall.domain.business_service.BusinessService;
import com.clickstechnology.Brillo.Mall.domain.business_service.BusinessServiceRepository;
import com.clickstechnology.Brillo.Mall.domain.product.Product;
import com.clickstechnology.Brillo.Mall.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicSearchIndexSyncService implements PublicSearchIndexSync {

    private final PublicSearchIndexRepository publicSearchIndexRepository;
    private final ProductRepository productRepository;
    private final BusinessServiceRepository businessServiceRepository;
    private final BusinessRepository businessRepository;
    private final CategoryCatalogService categoryCatalogService;

    @Override
    @Transactional
    public void syncBusiness(Business business) {
        upsertBusiness(business);
        refreshBusinessChildren(business);
    }

    @Override
    @Transactional
    public void syncProduct(Product product) {
        Business business = findBusiness(product.getBusinessId());
        upsertProduct(product, business);
    }

    @Override
    @Transactional
    public void syncBusinessService(BusinessService service) {
        Business business = findBusiness(service.getBusinessId());
        upsertService(service, business);
    }

    @Override
    @Transactional
    public void deleteBusinessTree(Business business) {
        syncBusiness(business);
    }

    private void refreshBusinessChildren(Business business) {
        List<Product> products = productRepository.findAllByBusinessId(business.getReference());
        List<BusinessService> services = businessServiceRepository.findAllByBusinessId(business.getReference());

        products.forEach(product -> upsertProduct(product, business));
        services.forEach(service -> upsertService(service, business));
    }

    private void upsertBusiness(Business business) {
        PublicSearchIndex index = publicSearchIndexRepository
                .findByReferenceAndType(business.getReference(), PublicSearchResultType.BUSINESS)
                .orElseGet(PublicSearchIndex::new);

        index.setReference(business.getReference());
        index.setType(PublicSearchResultType.BUSINESS);
        index.setBusinessId(business.getReference());
        index.setName(resolveBusinessName(business));
        index.setDescription(business.getDescription());
        index.setCategoryCode(business.getCategory() == null ? null : business.getCategory().name());
        index.setCategoryLabel(business.getCategory() == null ? null : business.getCategory().name());
        index.setBusinessName(null);
        index.setSlug(business.getSlug());
        index.setSku(null);
        index.setImageUrl(business.getLogoUrl());
        index.setLocation(formatLocation(business.getCity(), business.getState()));
        index.setPrice(null);
        index.setSearchText(buildSearchText(
                index.getName(),
                index.getDescription(),
                index.getCategoryCode(),
                index.getCategoryLabel(),
                index.getSlug(),
                index.getLocation(),
                business.getStorefrontName()));
        index.setActive(isBusinessVisible(business));
        index.setStatus(business.getStatus() == null ? EntityStatus.ACTIVE : business.getStatus());
        publicSearchIndexRepository.save(index);
    }

    private void upsertProduct(Product product, Business business) {
        PublicSearchIndex index = publicSearchIndexRepository
                .findByReferenceAndType(product.getReference(), PublicSearchResultType.PRODUCT)
                .orElseGet(PublicSearchIndex::new);

        String businessName = resolveBusinessName(business);
        String city = business == null ? null : business.getCity();
        String state = business == null ? null : business.getState();
        String businessSlug = business == null ? null : business.getSlug();
        String businessDescription = business == null ? null : business.getDescription();
        String ownerBusinessName = business == null ? null : business.getName();
        String categoryCode = product.getCategory();
        String categoryLabel = resolveCategoryLabel(BusinessCategory.PRODUCTS, categoryCode);
        index.setReference(product.getReference());
        index.setType(PublicSearchResultType.PRODUCT);
        index.setBusinessId(product.getBusinessId());
        index.setName(product.getName());
        index.setDescription(product.getDescription());
        index.setCategoryCode(categoryCode);
        index.setCategoryLabel(categoryLabel);
        index.setBusinessName(businessName);
        index.setSlug(null);
        index.setSku(product.getSku());
        index.setImageUrl(product.getMainImageUrl());
        index.setLocation(formatLocation(city, state));
        index.setPrice(product.getPrice());
        index.setSearchText(buildSearchText(
                product.getName(),
                product.getDescription(),
                product.getSku(),
                categoryCode,
                categoryLabel,
                ownerBusinessName,
                businessName,
                businessSlug,
                city,
                state,
                businessDescription));
        index.setActive(product.getStatus() == EntityStatus.ACTIVE && isBusinessVisible(business));
        index.setStatus(product.getStatus() == null ? EntityStatus.ACTIVE : product.getStatus());
        publicSearchIndexRepository.save(index);
    }

    private void upsertService(BusinessService service, Business business) {
        PublicSearchIndex index = publicSearchIndexRepository
                .findByReferenceAndType(service.getReference(), PublicSearchResultType.SERVICE)
                .orElseGet(PublicSearchIndex::new);

        String businessName = resolveBusinessName(business);
        String city = business == null ? null : business.getCity();
        String state = business == null ? null : business.getState();
        String businessSlug = business == null ? null : business.getSlug();
        String businessDescription = business == null ? null : business.getDescription();
        String ownerBusinessName = business == null ? null : business.getName();
        String categoryCode = service.getCategory();
        String categoryLabel = resolveCategoryLabel(BusinessCategory.SERVICES, categoryCode);
        index.setReference(service.getReference());
        index.setType(PublicSearchResultType.SERVICE);
        index.setBusinessId(service.getBusinessId());
        index.setName(service.getName());
        index.setDescription(service.getDescription());
        index.setCategoryCode(categoryCode);
        index.setCategoryLabel(categoryLabel);
        index.setBusinessName(businessName);
        index.setSlug(service.getSlug());
        index.setSku(null);
        index.setImageUrl(business == null ? null : business.getLogoUrl());
        index.setLocation(formatLocation(city, state));
        index.setPrice(service.getBasePrice());
        index.setSearchText(buildSearchText(
                service.getName(),
                service.getDescription(),
                service.getSlug(),
                categoryCode,
                categoryLabel,
                ownerBusinessName,
                businessName,
                businessSlug,
                city,
                state,
                businessDescription));
        index.setActive(service.getStatus() == EntityStatus.ACTIVE && isBusinessVisible(business));
        index.setStatus(service.getStatus() == null ? EntityStatus.ACTIVE : service.getStatus());
        publicSearchIndexRepository.save(index);
    }

    private boolean isBusinessVisible(Business business) {
        return business != null
                && business.getStatus() == EntityStatus.ACTIVE
                && Boolean.TRUE.equals(business.getIsActive())
                && Boolean.TRUE.equals(business.getStorefrontActive());
    }

    private Business findBusiness(String businessId) {
        return businessRepository.findByReference(businessId).orElse(null);
    }

    private String resolveBusinessName(Business business) {
        if (business == null) {
            return null;
        }
        if (business.getStorefrontName() != null && !business.getStorefrontName().isBlank()) {
            return business.getStorefrontName();
        }
        return business.getName();
    }

    private String resolveCategoryLabel(BusinessCategory categoryType, String categoryCode) {
        if (categoryCode == null || categoryCode.isBlank()) {
            return null;
        }

        Map<String, String> categoryLabels = categoryCatalogService.listCategories(categoryType)
                .stream()
                .collect(Collectors.toMap(
                        category -> category.getCode().toUpperCase(Locale.ENGLISH),
                        CategoryDto::getLabel,
                        (left, right) -> left));

        return categoryLabels.getOrDefault(categoryCode.toUpperCase(Locale.ENGLISH), categoryCode);
    }

    private String buildSearchText(String... values) {
        return java.util.Arrays.stream(values)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(value -> value.toLowerCase(Locale.ENGLISH))
                .collect(Collectors.joining(" "));
    }

    private String formatLocation(String city, String state) {
        if (city == null && state == null) {
            return null;
        }
        if (city == null || city.isBlank()) {
            return state;
        }
        if (state == null || state.isBlank()) {
            return city;
        }
        return city + ", " + state;
    }
}
