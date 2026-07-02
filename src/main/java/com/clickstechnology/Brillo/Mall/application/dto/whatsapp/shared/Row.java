package com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Row {

    private String id;
    private String title;
    private String description;
}
