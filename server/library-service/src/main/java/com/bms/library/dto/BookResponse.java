package com.bms.library.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponse {

    private Long id;

    private String title;

    private String isbn;

    private Long authorId;
    private String authorName;

    private Long categoryId;
    private String categoryName;

    private String publisher;

    private LocalDate printedOn;

    private int totalCopies;

    private int availableCopies;

    private int borrowedCopies;

    private String about;

    private boolean borrowed;
}
