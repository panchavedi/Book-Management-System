package com.bms.library.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenValidationResponse {

    private boolean valid;

    private String username;

    private Long userId;

    private String role;

    private String message;
}
