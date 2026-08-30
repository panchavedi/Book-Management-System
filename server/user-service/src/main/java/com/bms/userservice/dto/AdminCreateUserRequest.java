package com.bms.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCreateUserRequest {

    @NotBlank
    @Size(min = 3, max = 100)
    private String username;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    @NotBlank
    @Size(max = 150)
    private String fullName;

    @NotBlank
    @Pattern(regexp = "^[0-9+()\\- .]{7,30}$", message = "Phone number format is invalid")
    private String phone;

    @NotBlank
    @Size(max = 500)
    private String address;

    @NotBlank
    private String role;
}
