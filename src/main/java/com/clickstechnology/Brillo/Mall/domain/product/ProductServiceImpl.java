package com.clickstechnology.Brillo.Mall.domain.product;

import com.clickstechnology.Brillo.Mall.application.api.contracts.ProductService;
import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.product.AddProductRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.product.UpdateProductRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductSpecification productSpecification;

    @Override
    public void ensureProductNameDoesNotExist(String businessId, String name) {
        if (productRepository.existsByBusinessIdAndNameIgnoreCase(businessId, name)) {
            throw new BusinessException("Product with name '%s' already exists for this business."
                    .formatted(name));
        }
    }

    @Override
    public void ensureProductSkuDoesNotExist(String businessId, String sku) {
        if (productRepository.existsByBusinessIdAndSkuIgnoreCase(businessId, sku)) {
            throw new BusinessException("Product with SKU '%s' already exists for this business."
                    .formatted(sku));
        }
    }

    @Override
    public ProductDto createProduct(String businessId, AddProductRequest request, String defaultProductImageUrl) {
        Product product = Product.builder()
                .businessId(businessId)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .discountedPrice(request.getDiscountedPrice())
                .sku(request.getSku())
                .mainImageUrl(defaultProductImageUrl)
                .quantity(request.getQuantity())
                .status(EntityStatus.ACTIVE)
                .build();
        
        product = productRepository.save(product);
        return product.dto();
    }

    @Override
    public PaginatedResponse<ProductDto> getProducts(String businessId, Integer page, Integer pageSize) {
        return getProducts(businessId, page, pageSize, null, null, null, null, null);
    }

    @Override
    public PaginatedResponse<ProductDto> getProducts(
            String businessId,
            Integer page,
            Integer pageSize,
            String search,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStockOnly,
            EntityStatus status) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<Product> spec = productSpecification.getProducts(
                businessId,
                search,
                minPrice,
                maxPrice,
                inStockOnly,
                status);
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        List<ProductDto> productDtos = productPage.getContent().stream()
                .map(Product::dto)
                .collect(Collectors.toList());

        return PaginatedResponse.<ProductDto>builder()
                .items(productDtos)
                .page(productPage.getNumber() + 1)
                .perPage(productPage.getSize())
                .total(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .hasNext(productPage.hasNext())
                .hasPrevious(productPage.hasPrevious())
                .build();
    }

    @Override
    public ProductDto findProductByProductId(String productId) {
        return getByReference(productId)
                .dto();
    }

    private Product getByReference(String productId) {
        return productRepository.findByReference(productId)
                .orElseThrow(() -> new BusinessException("Product with ID: " + productId + " does not exist"));
    }

    @Override
    public ProductDto findProductBySku(String sku) {
        return getBySku(sku)
                .dto();
    }

    private Product getBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new BusinessException("Product with SKU: " + sku + " does not exist"));
    }

    @Override
    public void deleteProduct(String productId) {
        Product product = getByReference(productId);
        product.setStatus(EntityStatus.DELETED);
        productRepository.save(product);
    }

    @Override
    public ProductDto updateProduct(String productId, UpdateProductRequest request) {
        Product product = getByReference(productId);

        if (request == null) {
            return product.dto();
        }

        if (request.getName() != null) {
            product.setName(request.getName());
        }

        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }

        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }

        if (request.getDiscountedPrice() != null) {
            product.setDiscountedPrice(request.getDiscountedPrice());
        }

        if (request.getQuantity() != null) {
            product.setQuantity(request.getQuantity());
        }


        return productRepository.save(product).dto();
    }

    @Override
    public List<ProductDto> findProductsByIds(List<String> productIds) {
        return productRepository.findAllByReferenceIn(productIds).stream()
                .map(Product::dto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDto> findAllByProductIds(Set<String> productIds) {
        return productRepository.findAllByReferenceIn(List.copyOf(productIds)).stream()
                .map(Product::dto)
                .collect(Collectors.toList());
    }

    @Override
    public void checkInStock(Map<String, Integer> mappedProductQuantityMap) {
        Set<String> productIds = mappedProductQuantityMap.keySet();
        List<ProductDto> products = findAllByProductIds(productIds);

        for (ProductDto product : products) {
            if (product.getQuantity() < mappedProductQuantityMap.get(product.getId())) {
                throw new BusinessException("Insufficient stock for product : " + product.getName());
            }
        }

    }

    @Override
    public void reduceStock(Map<String, Integer> mappedProductQuantityMap) {
        Set<String> productIds = mappedProductQuantityMap.keySet();
        List<Product> products = productRepository.findAllByReferenceIn(List.copyOf(productIds));

        if (products.size() != productIds.size()) {
            throw new BusinessException("One or more products could not be found.");
        }

        for (Product product : products) {
            int requestedQuantity = mappedProductQuantityMap.get(product.getReference());
            int newQuantity = product.getQuantity() - requestedQuantity;

            if (newQuantity < 0) {
                throw new BusinessException("Insufficient stock for product: " + product.getName());
            }
            product.setQuantity(newQuantity);
        }

        productRepository.saveAll(products);
    }
}
