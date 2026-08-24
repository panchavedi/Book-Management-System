package com.bms.library.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "books",
        indexes = {
                @Index(
                        name = "idx_books_author",
                        columnList = "author_id"
                ),
                @Index(
                        name = "idx_books_category",
                        columnList = "category_id"
                ),
                @Index(
                        name = "idx_books_title",
                        columnList = "title"
                ),
                @Index(
                        name = "idx_books_available",
                        columnList = "available_copies"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_book_isbn",
                        columnNames = "isbn"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 250)
    private String title;

    @Column(nullable = false, length = 20)
    private String isbn;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "author_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_books_author"
            )
    )
    private Author author;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "category_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_books_category"
            )
    )
    private Category category;

    @Column(length = 200)
    private String publisher;

    @Column(name = "printed_on")
    private LocalDate printedOn;

    @Column(name = "total_copies", nullable = false)
    private int totalCopies;

    @Column(name = "available_copies", nullable = false)
    private int availableCopies;

    @Column(columnDefinition = "TEXT")
    private String about;

    /*
     * Optimistic locking safeguard.
     */
    @Version
    @Column(nullable = false)
    private Long version;

    @OneToMany(
            mappedBy = "book",
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Borrowing> borrowings = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }

    public void borrowCopy() {

        if (availableCopies <= 0) {
            throw new IllegalStateException(
                    "No available copies"
            );
        }

        availableCopies--;
    }

    public void returnCopy() {

        if (availableCopies >= totalCopies) {
            throw new IllegalStateException(
                    "All copies are already available"
            );
        }

        availableCopies++;
    }

    public int getBorrowedCopies() {

        return totalCopies - availableCopies;
    }
}