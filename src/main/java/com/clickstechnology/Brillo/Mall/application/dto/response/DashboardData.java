package com.clickstechnology.Brillo.Mall.application.dto.response;

import com.clickstechnology.Brillo.Mall.application.dto.StorefrontData;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardData {
    private UserDto user;
    private StorefrontData currentStorefrontData;
    private List<String> otherStores;
}
