package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.dto.search.PublicSearchResultDto;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.domain.publicsearch.PublicSearchIndex;
import com.clickstechnology.Brillo.Mall.domain.publicsearch.PublicSearchIndexRepository;
import com.clickstechnology.Brillo.Mall.domain.publicsearch.PublicSearchIndexSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicSearchService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final PublicSearchIndexRepository publicSearchIndexRepository;
    private final PublicSearchIndexSpecification publicSearchIndexSpecification;

    public PaginatedResponse<PublicSearchResultDto> search(String search, Integer page, Integer pageSize) {
        if (search == null || search.isBlank()) {
            throw new BusinessException("Search query cannot be blank.");
        }

        int normalizedPage = page == null || page < 1 ? DEFAULT_PAGE : page;
        int normalizedPageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;

        var pageable = PageRequest.of(
                normalizedPage - 1,
                normalizedPageSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        var pageResult = publicSearchIndexRepository.findAll(publicSearchIndexSpecification.search(search), pageable);
        List<PublicSearchResultDto> items = pageResult.getContent().stream()
                .map(PublicSearchIndex::dto)
                .toList();

        return PaginatedResponse.<PublicSearchResultDto>builder()
                .page(pageResult.getNumber() + 1)
                .perPage(pageResult.getSize())
                .total(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .hasNext(pageResult.hasNext())
                .hasPrevious(pageResult.hasPrevious())
                .items(items)
                .build();
    }
}
