package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.dto.search.PublicSearchResultDto;
import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;
import com.clickstechnology.Brillo.Mall.application.enums.PublicSearchResultType;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicSearchService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ProductService productService;
    private final BusinessService businessService;
    private final BusinessServiceService businessServiceService;
    private final CategoryCatalogService categoryCatalogService;

    public PaginatedResponse<PublicSearchResultDto> search(String search, Integer page, Integer pageSize) {
        if (search == null || search.isBlank()) {
            throw new BusinessException("Search query cannot be blank.");
        }

        int normalizedPage = page == null || page < 1 ? DEFAULT_PAGE : page;
        int normalizedPageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;

        List<ProductDto> products = productService.searchProducts(search);
        List<BusinessServiceDto> services = businessServiceService.searchServices(search);
        List<BusinessDto> businesses = businessService.searchBusinesses(search);

        Map<String, BusinessDto> businessesById = loadBusinessesById(products, services);
        Map<String, String> productCategoryLabels = categoryCatalogService.listCategories(BusinessCategory.PRODUCTS)
                .stream()
                .collect(Collectors.toMap(
                        category -> category.getCode().toUpperCase(),
                        category -> category.getLabel(),
                        (left, right) -> left,
                        HashMap::new));
        Map<String, String> serviceCategoryLabels = categoryCatalogService.listCategories(BusinessCategory.SERVICES)
                .stream()
                .collect(Collectors.toMap(
                        category -> category.getCode().toUpperCase(),
                        category -> category.getLabel(),
                        (left, right) -> left,
                        HashMap::new));

        List<PublicSearchResultDto> results = products.stream()
                .map(product -> toProductResult(product, businessesById, productCategoryLabels))
                .collect(Collectors.toCollection(java.util.ArrayList::new));
        results.addAll(services.stream()
                .map(service -> toServiceResult(service, businessesById, serviceCategoryLabels))
                .toList());
        results.addAll(businesses.stream()
                .map(this::toBusinessResult)
                .toList());

        results.sort(Comparator.comparing(
                PublicSearchResultDto::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));

        long total = results.size();
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / normalizedPageSize);
        int startIndex = Math.min((normalizedPage - 1) * normalizedPageSize, results.size());
        int endIndex = Math.min(startIndex + normalizedPageSize, results.size());
        List<PublicSearchResultDto> pageItems = results.subList(startIndex, endIndex);

        return PaginatedResponse.<PublicSearchResultDto>builder()
                .page(normalizedPage)
                .perPage(normalizedPageSize)
                .total(total)
                .totalPages(totalPages)
                .hasNext(normalizedPage < totalPages)
                .hasPrevious(normalizedPage > 1 && total > 0)
                .items(pageItems)
                .build();
    }

    private Map<String, BusinessDto> loadBusinessesById(List<ProductDto> products, List<BusinessServiceDto> services) {
        Set<String> businessIds = java.util.stream.Stream.concat(
                        products.stream().map(ProductDto::getBusinessId),
                        services.stream().map(BusinessServiceDto::getBusinessId))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (businessIds.isEmpty()) {
            return Map.of();
        }

        return businessService.findAllByBusinessIds(businessIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(BusinessDto::getId, businessDto -> businessDto));
    }

    private PublicSearchResultDto toProductResult(
            ProductDto product,
            Map<String, BusinessDto> businessesById,
            Map<String, String> categoryLabels) {
        BusinessDto business = businessesById.get(product.getBusinessId());
        return PublicSearchResultDto.builder()
                .type(PublicSearchResultType.PRODUCT)
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .category(resolveCategoryLabel(product.getCategory(), categoryLabels))
                .businessId(product.getBusinessId())
                .businessName(business == null ? null : business.getStorefrontName())
                .sku(product.getSku())
                .imageUrl(product.getMainImage())
                .price(product.getPrice())
                .createdAt(product.getCreatedAt())
                .build();
    }

    private PublicSearchResultDto toServiceResult(
            BusinessServiceDto service,
            Map<String, BusinessDto> businessesById,
            Map<String, String> categoryLabels) {
        BusinessDto business = businessesById.get(service.getBusinessId());
        return PublicSearchResultDto.builder()
                .type(PublicSearchResultType.SERVICE)
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .category(resolveCategoryLabel(service.getCategory(), categoryLabels))
                .businessId(service.getBusinessId())
                .businessName(business == null ? null : business.getStorefrontName())
                .slug(service.getSlug())
                .price(service.getBasePrice())
                .createdAt(service.getCreatedAt())
                .build();
    }

    private PublicSearchResultDto toBusinessResult(BusinessDto business) {
        String location = formatLocation(business.getCity(), business.getState());
        return PublicSearchResultDto.builder()
                .type(PublicSearchResultType.BUSINESS)
                .id(business.getId())
                .name(resolveBusinessName(business))
                .description(business.getDescription())
                .category(business.getCategory() == null ? null : business.getCategory().name())
                .businessId(business.getId())
                .slug(business.getSlug())
                .imageUrl(business.getLogoUrl())
                .location(location)
                .createdAt(business.getCreatedAt())
                .build();
    }

    private String resolveBusinessName(BusinessDto business) {
        if (business.getStorefrontName() != null && !business.getStorefrontName().isBlank()) {
            return business.getStorefrontName();
        }
        return business.getName();
    }

    private String resolveCategoryLabel(String categoryCode, Map<String, String> categoryLabels) {
        if (categoryCode == null) {
            return null;
        }
        return categoryLabels.getOrDefault(categoryCode.toUpperCase(), categoryCode);
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
