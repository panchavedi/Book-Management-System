package com.bms.library.repository;

import com.bms.library.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository
        extends JpaRepository<Author, Long> {

    boolean existsByNameIgnoreCase(String name);
}