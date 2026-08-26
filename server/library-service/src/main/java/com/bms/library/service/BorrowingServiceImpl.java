package com.bms.library.service;

import com.bms.library.dto.BorrowingResponse;
import com.bms.library.entity.Borrowing;
import com.bms.library.entity.BorrowingStatus;
import com.bms.library.exception.BookNotFoundException;
import com.bms.library.repository.BookRepository;
import com.bms.library.repository.BorrowingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowingServiceImpl
        implements BorrowingService {

    private final BorrowingRepository borrowingRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BorrowingResponse> findMyBorrowingHistory(
            Long borrowerId
    ) {

        return borrowingRepository
                .findBorrowingHistoryByBorrowerId(borrowerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowingResponse> findMyActiveBorrowings(
            Long borrowerId
    ) {

        return borrowingRepository
                .findActiveBorrowingsByBorrowerId(borrowerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowingResponse> findActiveBorrowersByBook(
            Long bookId
    ) {

        if (!bookRepository.existsById(bookId)) {
            throw new BookNotFoundException(bookId);
        }

        Sort sort =
                Sort.by(
                        Sort.Direction.ASC,
                        "borrowedOn"
                );

        return borrowingRepository
                .findByBookIdAndStatus(
                        bookId,
                        BorrowingStatus.BORROWED,
                        sort
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowingResponse> findAllActiveBorrowings(
            String sort
    ) {

        Sort.Direction direction =
                parseDirection(sort);

        Sort sorting =
                Sort.by(
                        direction,
                        "borrowedOn"
                );

        return borrowingRepository
                .findByStatus(
                        BorrowingStatus.BORROWED,
                        sorting
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Sort.Direction parseDirection(
            String sort
    ) {

        if (sort == null ||
                sort.isBlank() ||
                sort.equalsIgnoreCase("desc")) {

            return Sort.Direction.DESC;
        }

        if (sort.equalsIgnoreCase("asc")) {

            return Sort.Direction.ASC;
        }

        throw new IllegalArgumentException(
                "Invalid sort value. Use 'asc' or 'desc'."
        );
    }

    private BorrowingResponse toResponse(
            Borrowing borrowing
    ) {

        return BorrowingResponse.builder()
                .id(borrowing.getId())
                .bookId(
                        borrowing.getBook().getId()
                )
                .bookTitle(
                        borrowing.getBook().getTitle()
                )
                .borrowerId(
                        borrowing.getBorrowerId()
                )
                .borrowedOn(
                        borrowing.getBorrowedOn()
                )
                .returnedOn(
                        borrowing.getReturnedOn()
                )
                .status(
                        borrowing.getStatus()
                )
                .build();
    }
}
