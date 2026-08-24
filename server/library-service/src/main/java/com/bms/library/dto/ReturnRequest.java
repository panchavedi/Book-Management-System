package com.bms.library.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnRequest {

    @NotNull(message = "Borrower ID is required")
    @Positive(message = "Borrower ID must be positive")
    private Long borrowerId;
}
