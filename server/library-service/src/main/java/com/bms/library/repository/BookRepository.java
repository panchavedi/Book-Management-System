package com.bms.library.repository;

import com.bms.library.entity.Book;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository
        extends JpaRepository<Book, Long> {

    @EntityGraph(attributePaths = {"author", "category"})
    @Override
    List<Book> findAll();

    @EntityGraph(attributePaths = {"author", "category"})
    Optional<Book> findWithAuthorAndCategoryById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT b
        FROM Book b
        JOIN FETCH b.author
        JOIN FETCH b.category
        WHERE b.id = :id
    """)
    Optional<Book> findByIdForUpdate(
            @Param("id") Long id
    );

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(
            String isbn,
            Long id
    );

    @Query("""
        SELECT DISTINCT b
        FROM Book b
        JOIN FETCH b.author
        JOIN FETCH b.category
        WHERE
            LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(COALESCE(b.publisher, ''))
                   LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(b.author.name)
                   LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(b.category.name)
                   LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    List<Book> search(
            @Param("keyword") String keyword
    );

    @Query("""
        SELECT DISTINCT b
        FROM Book b
        JOIN FETCH b.author
        JOIN FETCH b.category
        WHERE b.availableCopies > 0
    """)
    List<Book> findAllAvailable();

    @Query("""
        SELECT DISTINCT b
        FROM Book b
        JOIN FETCH b.author
        JOIN FETCH b.category
        WHERE b.author.id = :authorId
    """)
    List<Book> findByAuthorId(
            @Param("authorId") Long authorId
    );
}