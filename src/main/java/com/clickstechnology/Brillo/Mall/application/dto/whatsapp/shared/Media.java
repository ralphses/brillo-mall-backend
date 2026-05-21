package com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Media {
    private String id;
    private String link;
    private String caption;
}
