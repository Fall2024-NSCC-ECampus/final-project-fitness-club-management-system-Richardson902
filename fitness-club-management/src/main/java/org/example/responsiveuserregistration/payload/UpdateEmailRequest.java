package org.example.responsiveuserregistration.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateEmailRequest {

    @NotBlank(message = "Email is required")
    @Pattern(regexp = "(?i)^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z]{2,6}$", message = "Email must be valid")
    private String email;
}
