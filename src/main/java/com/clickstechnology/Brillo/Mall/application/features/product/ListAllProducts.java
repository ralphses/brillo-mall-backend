package com.clickstechnology.Brillo.Mall.application.features.product;

import com.clickstechnology.Brillo.Mall.application.api.contracts.AuthenticationUtil;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.ProductService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.infrastructure.logging.LoggableRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListAllProducts {

    private final ProductService productService;
    private final BusinessService businessService;
    private final AuthenticationUtil authenticationUtil;
    private final UserService userService;


    /**
     * Executes the process of retrieving a paginated list of products for a specific business.
     *
     * @param businessId         The unique identifier of the business.
     * @return A {@link PaginatedResponse} containing the list of products.
     */
    public PaginatedResponse<ProductDto> execute(String businessId, Integer page, Integer pageSize) {
        log.info(":::Attempting to get products for businessId: {}", businessId);
        return productService.getProducts(businessId, page, pageSize);

    }
}