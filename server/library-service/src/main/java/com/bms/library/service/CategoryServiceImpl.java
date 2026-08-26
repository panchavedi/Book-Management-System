package com.bms.library.service;

import com.bms.library.dto.CategoryRequest;
import com.bms.library.dto.CategoryResponse;
import com.bms.library.entity.Category;
import com.bms.library.exception.CategoryNotFoundException;
import com.bms.library.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponse create(CategoryRequest request) {

        String name = request.getName().trim();

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException(
                    "Category already exists with name: " + name
            );
        }

        Category category = Category.builder()
                .name(name)
                .build();

        return toResponse(
                categoryRepository.save(category)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {

        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(
                        () -> new CategoryNotFoundException(id)
                );

        return toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> search(
            String keyword
    ) {

        if (keyword == null ||
                keyword.trim().isEmpty()) {

            return findAll();
        }

        return categoryRepository
                .findByNameContainingIgnoreCase(
                        keyword.trim()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CategoryResponse update(
            Long id,
            CategoryRequest request
    ) {

        Category category =
                categoryRepository.findById(id)
                        .orElseThrow(
                                () -> new CategoryNotFoundException(id)
                        );

        String name = request.getName().trim();

        if (!category.getName().equalsIgnoreCase(name)
                && categoryRepository.existsByNameIgnoreCase(name)) {

            throw new IllegalArgumentException(
                    "Category already exists with name: " + name
            );
        }

        category.setName(name);

        return toResponse(category);
    }

    @Override
    public void delete(Long id) {

        Category category =
                categoryRepository.findById(id)
                        .orElseThrow(
                                () -> new CategoryNotFoundException(id)
                        );
        categoryRepository.delete(category);
    }

    private CategoryResponse toResponse(
            Category category
    ) {

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
