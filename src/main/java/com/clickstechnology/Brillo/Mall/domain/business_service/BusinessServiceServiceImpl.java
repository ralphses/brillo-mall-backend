package com.clickstechnology.Brillo.Mall.domain.business_service;

import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessServiceService;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.AddBusinessServiceRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.UpdateBusinessServiceRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.enums.PricingType;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.application.utils.AppUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
class BusinessServiceServiceImpl implements BusinessServiceService {

    private final BusinessServiceRepository businessServiceRepository;

    @Override
    public BusinessServiceDto addService(AddBusinessServiceRequest request, UserDto userDto) {

        String slug = AppUtils.generateSlug(request.getName());
        if (businessServiceRepository.existsByBusinessIdAndSlug(request.getBusinessId(), slug)) {
            throw new BusinessException("A service with this name already exists for your business.");
        }

        BusinessService newService = BusinessService.builder()
                .businessId(request.getBusinessId())
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .category(request.getCategory())
                .pricingType(request.getPricingType())
                .basePrice(request.getBasePrice())
                .durationMinutes(request.getDurationMinutes())
                .negotiable(request.getNegotiable())
                .requiresSchedule(request.getRequiresSchedule())
                .build();

        return businessServiceRepository.save(newService).dto();
    }

    @Override
    public BusinessServiceDto updateService(String serviceId, UpdateBusinessServiceRequest request, UserDto userDto) {
        BusinessService serviceToUpdate = businessServiceRepository.findByServiceId(serviceId)
                .orElseThrow(() -> new BusinessException("Service not found."));

       if (request.getName() != null) {
           serviceToUpdate.setName(request.getName());
       }
       if (request.getDescription() != null) {
           serviceToUpdate.setDescription(request.getDescription());
       }
       if (request.getCategory() != null) {
           serviceToUpdate.setCategory(request.getCategory());
       }
       if (request.getPricingType() != null) {
           serviceToUpdate.setPricingType(request.getPricingType());
       }
       if (request.getBasePrice() != null) {
           serviceToUpdate.setBasePrice(request.getBasePrice());
       }
       if (request.getDurationMinutes() != null) {
           serviceToUpdate.setDurationMinutes(request.getDurationMinutes());
       }

       if (request.getNegotiable() != null) {
           serviceToUpdate.setNegotiable(request.getNegotiable());
       }

       if (request.getRequiresSchedule() != null) {
           serviceToUpdate.setRequiresSchedule(request.getRequiresSchedule());
       }

        return businessServiceRepository.save(serviceToUpdate).dto();
    }

    @Override
    public PaginatedResponse<BusinessServiceDto> listServices(String businessId, Pageable pageable) {
        Page<BusinessService> page;
        if (businessId != null && !businessId.isBlank()) {
            page = businessServiceRepository.findByBusinessId(businessId, pageable);
        } else {
            page = businessServiceRepository.findAll(pageable);
        }

        List<BusinessServiceDto> dtos = page.getContent().stream()
                .map(BusinessService::dto)
                .collect(Collectors.toList());


        return PaginatedResponse.<BusinessServiceDto>builder()
                .page(page.getNumber() + 1)
                .perPage(page.getSize())
                .total((int) page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .items(dtos)
                .build();
    }

    @Override
    public BusinessServiceDto getByIdOrSlug(String serviceIdOrSlug) {
        return businessServiceRepository.findByServiceIdOrSlug(serviceIdOrSlug)
                .map(BusinessService::dto)
                .orElseThrow(() -> new BusinessException("Business service not found."));
    }

    @Override
    @Transactional
    public void deleteBusinessService(String businessServiceId) {
        BusinessService businessService = findByBusinessId(businessServiceId);
        businessService.setStatus(EntityStatus.DELETED);
        businessServiceRepository.save(businessService);
    }

    private BusinessService findByBusinessId(String businessServiceId) {
        return businessServiceRepository.findByServiceId(businessServiceId)
                .orElseThrow(() -> new BusinessException("Business service not found."));
    }

    @Override
    public BusinessServiceDto findById(String businessServiceId) {
        return findByBusinessId(businessServiceId).dto();
    }

    @Override
    public void validateForRequests(BusinessServiceDto businessService) {
        boolean isFitForNegotiation = businessService.isNegotiable()
                && PricingType.NEGOTIABLE == businessService.getPricingType()
                && businessService.isActive();

        if (!isFitForNegotiation) {
            throw new BusinessException("This business service is not valid or not negotiable.");
        }
    }

    @Override
    public void validatePriceAgreed(BusinessServiceDto businessService, BigDecimal totalPrice) {
        if (totalPrice.compareTo(businessService.getBasePrice()) < 0) {
            throw new BusinessException("Total price not accepted.");
        }
    }
}