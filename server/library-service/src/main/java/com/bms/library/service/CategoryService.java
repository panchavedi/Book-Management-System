package com.bms.library.service;

import com.bms.library.dto.CategoryRequest;
import com.bms.library.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse create(CategoryRequest request);

    List<CategoryResponse> findAll();

    CategoryResponse findById(Long id);

    List<CategoryResponse> search(String keyword);

    CategoryResponse update(
            Long id,
            CategoryRequest request
    );

    void delete(Long id);
}