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
public class ButtonAction {

    private List<Button> buttons;
}