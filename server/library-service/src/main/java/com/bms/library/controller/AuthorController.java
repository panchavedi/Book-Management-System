package com.bms.library.controller;

import com.bms.library.dto.AuthorRequest;
import com.bms.library.dto.AuthorResponse;
import com.bms.library.service.AuthorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @PostMapping
    public ResponseEntity<AuthorResponse> create(
            @Valid @RequestBody AuthorRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authorService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<AuthorResponse>> findAll() {
        return ResponseEntity.ok(authorService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponse> findById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                authorService.findById(id)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<List<AuthorResponse>> search(
            @RequestParam String keyword
    ) {

        return ResponseEntity.ok(
                authorService.search(keyword)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuthorResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AuthorRequest request
    ) {
        return ResponseEntity.ok(
                authorService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        authorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}