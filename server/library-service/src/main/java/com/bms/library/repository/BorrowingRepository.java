package com.bms.library.repository;

import com.bms.library.entity.Borrowing;
import com.bms.library.entity.BorrowingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BorrowingRepository
        extends JpaRepository<Borrowing, Long> {

    boolean existsByBookIdAndBorrowerIdAndStatus(
            Long bookId,
            Long borrowerId,
            BorrowingStatus status
    );

    boolean existsByBookIdAndStatus(
            Long bookId,
            BorrowingStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT br
        FROM Borrowing br
        WHERE br.book.id = :bookId
          AND br.borrowerId = :borrowerId
          AND br.status = com.bms.library.entity.BorrowingStatus.BORROWED
    """)
    Optional<Borrowing> findActiveBorrowingForUpdate(
            @Param("bookId") Long bookId,
            @Param("borrowerId") Long borrowerId
    );

    @Query("""
        SELECT br
        FROM Borrowing br
        JOIN FETCH br.book b
        WHERE br.borrowerId = :borrowerId
        ORDER BY br.borrowedOn DESC
    """)
    List<Borrowing> findBorrowingHistoryByBorrowerId(
            @Param("borrowerId") Long borrowerId
    );

    @Query("""
        SELECT br
        FROM Borrowing br
        JOIN FETCH br.book b
        WHERE br.borrowerId = :borrowerId
          AND br.status = com.bms.library.entity.BorrowingStatus.BORROWED
        ORDER BY br.borrowedOn DESC
    """)
    List<Borrowing> findActiveBorrowingsByBorrowerId(
            @Param("borrowerId") Long borrowerId
    );

    @Query("""
        SELECT br
        FROM Borrowing br
        JOIN FETCH br.book b
        WHERE br.book.id = :bookId
        ORDER BY br.borrowedOn DESC
    """)
    List<Borrowing> findBorrowingHistoryByBookId(
            @Param("bookId") Long bookId
    );

    @Query("""
        SELECT br
        FROM Borrowing br
        JOIN FETCH br.book b
        ORDER BY br.borrowedOn DESC
    """)
    List<Borrowing> findAllBorrowingHistory();

    List<Borrowing> findByBookIdAndStatus(
            Long bookId,
            BorrowingStatus status,
            Sort sort
    );

    List<Borrowing> findByStatus(
            BorrowingStatus status,
            Sort sort
    );
}
