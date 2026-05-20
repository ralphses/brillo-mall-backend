package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.OnboardBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.UpdateBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;

import java.util.List;
import java.util.Set;

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

    void validateBusinessIsActive(Set<String> allProductOwners);

    void addCustomer(CustomerDto customer, Set<String> businessIds);

    Set<String> findBusinessCustomers(String businessId);

    List<BusinessDto> findAllByOwnerId(String userId);

    List<BusinessDto> findAllByBusinessIds(Set<String> businessIds);

}