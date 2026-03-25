package com.clickstechnology.Brillo.Mall.application.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChangePasswordRequest {
    @NotEmpty(message = "Password is required")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{8,64}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one number"
    )
    private final String currentPassword;

    @NotEmpty(message = "New password is required")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{8,64}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one number"
    )
    private final String newPassword;

    @NotEmpty(message = "Confirm new password is required")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{8,64}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one number"
    )
    private final String confirmNewPassword;
}
