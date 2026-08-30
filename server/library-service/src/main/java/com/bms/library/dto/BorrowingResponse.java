package com.bms.library.dto;

import com.bms.library.entity.BorrowingStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowingResponse {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private Long borrowerId;
    private UserResponse borrower;
    private LocalDateTime borrowedOn;
    private LocalDateTime returnedOn;
    private BorrowingStatus status;
}
