package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.OnboardBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.UpdateBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;

import java.util.List;

public interface BusinessService {
    void ensureBusinessNameDoesNotExist(String businessName);

    void ensureBusinessBelongsToUser(String businessId, String userId);
    void createNew(OnboardBusinessRequest request, UserDto user, String logoUrl, BusinessCategory businessCategory);

    BusinessDto findByBusinessId(String businessId);

    BusinessDto findByBusinessSlug(String businessSlug);

    boolean existsByBusinessId(String businessId);

    void addLogo(String businessId, String logoUrl);

    void updateBusiness(String businessId, UpdateBusinessRequest updateBusinessRequest);

    PaginatedResponse<BusinessDto> findAllByOwnerId(String userId, Integer page, Integer pageSize);
}