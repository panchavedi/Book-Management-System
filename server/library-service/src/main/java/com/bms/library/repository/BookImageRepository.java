package com.bms.library.repository;

import com.bms.library.entity.BookImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookImageRepository extends JpaRepository<BookImage, Long> {

    List<BookImage> findByBookIdOrderByDisplayOrderAsc(Long bookId);

    Optional<BookImage> findByIdAndBookId(Long id, Long bookId);

    long countByBookId(Long bookId);

    void deleteByBookId(Long bookId);
}
