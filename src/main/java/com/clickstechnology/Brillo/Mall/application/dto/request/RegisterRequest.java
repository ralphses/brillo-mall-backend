package com.clickstechnology.Brillo.Mall.application.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotEmpty(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Pattern(
            regexp = "^[A-Za-z]+(?: [A-Za-z]+)*$",
            message = "Full name must contain only alphabets and spaces"
    )
    private String fullName;

    @NotEmpty(message = "Email or phone number is required")
    @Pattern(
            regexp = "^(?:\\+?\\d{10,15}|[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,})$",
            message = "Kindly provide a valid email or phone number"
    )
    private String emailOrPhone;

    @NotEmpty(message = "Password is required")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{8,64}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one number"
    )
    private String password;
    private String inviteCode;
}
