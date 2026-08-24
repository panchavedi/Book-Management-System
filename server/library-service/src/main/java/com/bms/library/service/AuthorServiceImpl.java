package com.bms.library.service;

import com.bms.library.dto.AuthorRequest;
import com.bms.library.dto.AuthorResponse;
import com.bms.library.entity.Author;
import com.bms.library.exception.AuthorNotFoundException;
import com.bms.library.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    @Override
    public AuthorResponse create(AuthorRequest request) {

        String name = request.getName().trim();

        if (authorRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException(
                    "Author already exists with name: " + name
            );
        }

        Author author = Author.builder()
                .name(name)
                .build();

        return toResponse(authorRepository.save(author));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuthorResponse> findAll() {

        return authorRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthorResponse findById(Long id) {

        Author author = authorRepository.findById(id)
                .orElseThrow(
                        () -> new AuthorNotFoundException(id)
                );

        return toResponse(author);
    }

    @Override
    public AuthorResponse update(
            Long id,
            AuthorRequest request
    ) {

        Author author = authorRepository.findById(id)
                .orElseThrow(
                        () -> new AuthorNotFoundException(id)
                );

        String name = request.getName().trim();

        if (!author.getName().equalsIgnoreCase(name)
                && authorRepository.existsByNameIgnoreCase(name)) {

            throw new IllegalArgumentException(
                    "Author already exists with name: " + name
            );
        }

        author.setName(name);

        return toResponse(author);
    }

    @Override
    public void delete(Long id) {

        Author author = authorRepository.findById(id)
                .orElseThrow(
                        () -> new AuthorNotFoundException(id)
                );

        authorRepository.delete(author);
    }

    private AuthorResponse toResponse(Author author) {

        return AuthorResponse.builder()
                .id(author.getId())
                .name(author.getName())
                .build();
    }
}
