package com.bms.library.repository;

import com.bms.library.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthorRepository
        extends JpaRepository<Author, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Author> findByNameContainingIgnoreCase(String keyword);
}