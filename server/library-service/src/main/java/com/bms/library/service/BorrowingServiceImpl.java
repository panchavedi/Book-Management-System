package com.bms.library.service;

import com.bms.library.dto.BorrowingResponse;
import com.bms.library.dto.UserResponse;
import com.bms.library.entity.Borrowing;
import com.bms.library.entity.BorrowingStatus;
import com.bms.library.exception.BookNotFoundException;
import com.bms.library.feign.UserInterface;
import com.bms.library.repository.BookRepository;
import com.bms.library.repository.BorrowingRepository;
import feign.FeignException;
import com.bms.library.exception.UserProfileNotFoundException;
import com.bms.library.exception.UserServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BorrowingServiceImpl
        implements BorrowingService {

    private final BorrowingRepository borrowingRepository;
    private final BookRepository bookRepository;
    private final UserInterface userInterface;

    @Override
    @Transactional(readOnly = true)
    public List<BorrowingResponse> findMyBorrowingHistory(
            Long borrowerId
    ) {
        Map<Long, UserResponse> userCache = new HashMap<>();

        return borrowingRepository
                .findBorrowingHistoryByBorrowerId(borrowerId)
                .stream()
                .map(borrowing -> toResponse(borrowing, userCache))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowingResponse> findMyActiveBorrowings(
            Long borrowerId
    ) {
        Map<Long, UserResponse> userCache = new HashMap<>();

        return borrowingRepository
                .findActiveBorrowingsByBorrowerId(borrowerId)
                .stream()
                .map(borrowing -> toResponse(borrowing, userCache))
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

        Sort sort = Sort.by(
                Sort.Direction.ASC,
                "borrowedOn"
        );

        Map<Long, UserResponse> userCache = new HashMap<>();

        return borrowingRepository
                .findByBookIdAndStatus(
                        bookId,
                        BorrowingStatus.BORROWED,
                        sort
                )
                .stream()
                .map(borrowing -> toResponse(borrowing, userCache))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowingResponse> findAllActiveBorrowings(
            String sort
    ) {
        Sort.Direction direction = parseDirection(sort);

        Sort sorting = Sort.by(
                direction,
                "borrowedOn"
        );

        Map<Long, UserResponse> userCache = new HashMap<>();

        return borrowingRepository
                .findByStatus(
                        BorrowingStatus.BORROWED,
                        sorting
                )
                .stream()
                .map(borrowing -> toResponse(borrowing, userCache))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowingResponse> findBorrowingHistoryByBook(
            Long bookId
    ) {
        if (!bookRepository.existsById(bookId)) {
            throw new BookNotFoundException(bookId);
        }

        Map<Long, UserResponse> userCache = new HashMap<>();

        return borrowingRepository
                .findBorrowingHistoryByBookId(bookId)
                .stream()
                .map(borrowing -> toResponse(borrowing, userCache))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowingResponse> findAllBorrowingHistory() {
        Map<Long, UserResponse> userCache = new HashMap<>();

        return borrowingRepository
                .findAllBorrowingHistory()
                .stream()
                .map(borrowing -> toResponse(borrowing, userCache))
                .toList();
    }

    private Sort.Direction parseDirection(String sort) {

        if (sort == null
                || sort.isBlank()
                || sort.equalsIgnoreCase("desc")) {

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
            Borrowing borrowing,
            Map<Long, UserResponse> userCache
    ) {
        Long borrowerId = borrowing.getBorrowerId();

        UserResponse borrower =
                userCache.computeIfAbsent(
                        borrowerId,
                        this::getUserDetails
                );

        return BorrowingResponse.builder()
                .id(borrowing.getId())
                .bookId(borrowing.getBook().getId())
                .bookTitle(borrowing.getBook().getTitle())
                .borrowerId(borrowerId)
                .borrower(borrower)
                .borrowedOn(borrowing.getBorrowedOn())
                .returnedOn(borrowing.getReturnedOn())
                .status(borrowing.getStatus())
                .build();
    }

    /**
     * Uses the incoming request's authenticated Bearer token through
     * UserFeignConfiguration. Incomplete borrower records are never returned.
     */
    private UserResponse getUserDetails(Long userId) {
        try {
            UserResponse response =
                    userInterface.getUserById(userId).getBody();

            if (response == null) {
                throw new IllegalStateException(
                        "User Service returned an empty profile for user "
                                + userId
                );
            }

            return response;

        } catch (FeignException.NotFound e) {
            throw new UserProfileNotFoundException(userId, e);
        } catch (FeignException.Unauthorized e) {
            throw new UserServiceUnavailableException(
                    "User Service rejected the propagated authentication while retrieving user " + userId,
                    e
            );
        } catch (FeignException.Forbidden e) {
            throw new UserServiceUnavailableException(
                    "User Service denied access while retrieving user " + userId,
                    e
            );
        } catch (FeignException e) {
            throw new UserServiceUnavailableException(
                    "Unable to retrieve user "
                            + userId
                            + " from User Service. HTTP status: "
                            + e.status(),
                    e
            );
        } catch (RuntimeException e) {
            throw new UserServiceUnavailableException(
                    "User Service is unavailable while retrieving user " + userId,
                    e
            );
        }
    }
}
