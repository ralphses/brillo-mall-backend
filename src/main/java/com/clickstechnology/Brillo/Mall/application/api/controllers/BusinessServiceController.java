package com.clickstechnology.Brillo.Mall.application.api.controllers;

import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.AddBusinessServiceRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.UpdateBusinessServiceRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.ResponseWrapper;
import com.clickstechnology.Brillo.Mall.application.features.business.AddBusinessService;
import com.clickstechnology.Brillo.Mall.application.features.business.ManageBusinessService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.clickstechnology.Brillo.Mall.application.dto.response.ResponseBuilder.success;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/business-service")
public class BusinessServiceController {

    private final AddBusinessService addBusinessService;
    private final ManageBusinessService manageBusinessService;

    @PostMapping
    public ResponseWrapper<String> addBusinessService(
            @RequestBody @Valid final AddBusinessServiceRequest request,
            final HttpServletRequest httpServletRequest) {
        addBusinessService.execute(request, httpServletRequest);
        return success("Business service added successfully");
    }

    @PutMapping("/{serviceId}")
    public ResponseWrapper<String> updateBusinessService(
            @PathVariable final String serviceId,
            @RequestBody @Valid final UpdateBusinessServiceRequest request,
            final HttpServletRequest httpServletRequest) {
        return success("Business service updated successfully");
    }

    @GetMapping
    public ResponseWrapper<PaginatedResponse<BusinessServiceDto>> listBusinessServices(
            @RequestParam(required = false) final String businessId,
            @RequestParam(defaultValue = "1") final Integer page,
            @RequestParam(defaultValue = "10") final Integer pageSize) {
        return success(manageBusinessService.listBusinessServices(businessId, page, pageSize));
    }

    @GetMapping("{serviceId}")
    public ResponseWrapper<BusinessServiceDto> getBusinessService(
            @PathVariable final String serviceId,
            final HttpServletRequest httpServletRequest) {
        return success(manageBusinessService.getOneService(serviceId, httpServletRequest));
    }

    @DeleteMapping("{serviceId}")
    public ResponseWrapper<String> deleteBusinessService(
            @PathVariable final String serviceId,
            final HttpServletRequest httpServletRequest) {
        manageBusinessService.deleteById(serviceId, httpServletRequest);
        return success("Service deleted successfully");
    }

}