package com.bms.library.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "borrowings",
        indexes = {
                @Index(
                        name = "idx_borrowings_book",
                        columnList = "book_id"
                ),
                @Index(
                        name = "idx_borrowings_borrower",
                        columnList = "borrower_id"
                ),
                @Index(
                        name = "idx_borrowings_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Borrowing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "book_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_borrowings_book"
            )
    )
    private Book book;

    /*
     * Owned by user-service.
     * No local FK or JPA relationship.
     */
    @Column(
            name = "borrower_id",
            nullable = false
    )
    private Long borrowerId;

    @Column(
            name = "borrowed_on",
            nullable = false
    )
    private LocalDateTime borrowedOn;

    @Column(name = "returned_on")
    private LocalDateTime returnedOn;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private BorrowingStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (borrowedOn == null) {
            borrowedOn = now;
        }

        if (status == null) {
            status = BorrowingStatus.BORROWED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void markReturned() {

        if (status != BorrowingStatus.BORROWED) {
            throw new IllegalStateException(
                    "Borrowing is not currently active"
            );
        }

        status = BorrowingStatus.RETURNED;
        returnedOn = LocalDateTime.now();
    }
}