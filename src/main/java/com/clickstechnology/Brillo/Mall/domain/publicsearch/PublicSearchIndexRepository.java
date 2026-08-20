package com.clickstechnology.Brillo.Mall.domain.publicsearch;

import com.clickstechnology.Brillo.Mall.application.enums.PublicSearchResultType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PublicSearchIndexRepository extends JpaRepository<PublicSearchIndex, Long>,
        JpaSpecificationExecutor<PublicSearchIndex> {

    Optional<PublicSearchIndex> findByReferenceAndType(String reference, PublicSearchResultType type);

    List<PublicSearchIndex> findAllByBusinessId(String businessId);

    List<PublicSearchIndex> findAllByBusinessIdAndType(String businessId, PublicSearchResultType type);
}
