package com.clickstechnology.Brillo.Mall.application.api.controllers;

import com.clickstechnology.Brillo.Mall.application.api.contracts.CustomerService;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.ResponseWrapper;
import com.clickstechnology.Brillo.Mall.application.features.customer.ManageCustomer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.clickstechnology.Brillo.Mall.application.dto.response.ResponseBuilder.success;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/customers")
@Tag(name = "Customers", description = "Customer management APIs")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    private final ManageCustomer manageCustomer;

    @GetMapping
    @Operation(summary = "List customers")
    public ResponseWrapper<PaginatedResponse<CustomerDto>> getAllCustomers(
            final HttpServletRequest httpServletRequest,
            @RequestParam(value = "businessId", required = false) String businessId,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize) {
        var customers = manageCustomer.getAllCustomers(httpServletRequest, businessId, page, pageSize);
        return success(customers);
    }
}
