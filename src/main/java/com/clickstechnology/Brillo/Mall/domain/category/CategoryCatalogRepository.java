package com.clickstechnology.Brillo.Mall.domain.category;

import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;
import com.clickstechnology.Brillo.Mall.infrastructure.persistence.JpaAuditor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface CategoryCatalogRepository extends JpaRepository<CategoryCatalog, Long> {

    List<CategoryCatalog> findAllByCategoryTypeAndRecordStatusOrderByDisplayOrderAscLabelAsc(
            BusinessCategory categoryType,
            JpaAuditor.RecordStatus recordStatus);
}
