package com.bms.library.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 250, message = "Title cannot exceed 250 characters")
    private String title;

    @NotBlank(message = "ISBN is required")
    @Size(max = 20, message = "ISBN cannot exceed 20 characters")
    private String isbn;

    @NotNull(message = "Author ID is required")
    @Positive(message = "Author ID must be positive")
    private Long authorId;

    @NotNull(message = "Category ID is required")
    @Positive(message = "Category ID must be positive")
    private Long categoryId;

    @Size(max = 200)
    private String publisher;

    @PastOrPresent(message = "Printed date cannot be in the future")
    private LocalDate printedOn;

    @Min(
            value = 1,
            message = "Total copies must be at least 1"
    )
    private int totalCopies;

    @Size(max = 5000)
    private String about;
}
