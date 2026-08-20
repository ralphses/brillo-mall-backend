package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.domain.business.Business;
import com.clickstechnology.Brillo.Mall.domain.business_service.BusinessService;
import com.clickstechnology.Brillo.Mall.domain.product.Product;

public interface PublicSearchIndexSync {
    void syncBusiness(Business business);

    void syncProduct(Product product);

    void syncBusinessService(BusinessService service);

    void deleteBusinessTree(Business business);
}
