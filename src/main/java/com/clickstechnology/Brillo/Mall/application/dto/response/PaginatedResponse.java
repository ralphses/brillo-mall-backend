/*
 * ------------------------------------------------------------------------------
 * Copyright (c) 2025 9 Payment Service Bank
 * ------------------------------------------------------------------------------
 * This code is the property of 9 Payment Service Bank. Unauthorized copying,
 * sharing, or use of this code, via any medium, is strictly prohibited
 * without express permission from of 9 Payment Service Bank.
 * ------------------------------------------------------------------------------
 * @package    ng.com.ninepsb.dto.response
 * @author     Eze.Raphael
 * @license    Proprietary
 * @version    1.0.0
 * @link       https://www.9psb.com.ng
 */
package com.clickstechnology.Brillo.Mall.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic wrapper for paginated API responses.
 *
 * @param <T> the type of items in the response
 *
 * @author Eze.Raphael
 * @version 1.0.0
 * @since 1.0.0
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedResponse<T> {

    /** Current page number (1-based). */
    private int page;

    /** Number of items per page. */
    private int perPage;

    /** Total number of items across all pages. */
    private long total;

    /** Total number of pages. */
    private int totalPages;

    /** Whether there is a next page. */
    private boolean hasNext;

    /** Whether there is a previous page. */
    private boolean hasPrevious;

    /** Items contained in the current page. */
    private List<T> items;
}