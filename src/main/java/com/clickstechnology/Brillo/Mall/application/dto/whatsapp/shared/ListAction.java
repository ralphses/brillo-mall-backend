package com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListAction {

    private String button;
    private List<Section> sections;
}
