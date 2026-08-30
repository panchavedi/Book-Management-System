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
public class UserUpdateRequest {

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @NotBlank
    @Size(max = 150)
    private String fullName;

    @NotBlank
    @Pattern(regexp = "^[0-9+()\\- .]{7,30}$", message = "Phone number format is invalid")
    private String phone;

    @NotBlank
    @Size(max = 500)
    private String address;
}
