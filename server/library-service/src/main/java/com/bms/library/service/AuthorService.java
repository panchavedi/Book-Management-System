package com.bms.library.service;

import com.bms.library.dto.AuthorRequest;
import com.bms.library.dto.AuthorResponse;

import java.util.List;

public interface AuthorService {

    AuthorResponse create(AuthorRequest request);

    List<AuthorResponse> findAll();

    AuthorResponse findById(Long id);

    AuthorResponse update(
            Long id,
            AuthorRequest request
    );

    void delete(Long id);
}
