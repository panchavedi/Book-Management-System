package com.bms.library.controller;

import com.bms.library.dto.*;
import com.bms.library.service.BookService;
import com.bms.library.security.CurrentUser;
import com.bms.library.service.BookImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BooksController {

    private final BookService bookService;
    private final BookImageService bookImageService;

    @PostMapping
    public ResponseEntity<BookResponse> create(
            @Valid @RequestBody BookRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bookService.create(request));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookResponse> createMultipart(
            @Valid @RequestPart("book") BookRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bookService.create(request, images));
    }

    @PostMapping(value = "/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookResponse> createWithImages(
            @Valid @RequestPart("book") BookRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bookService.create(request, images));
    }

    @GetMapping
    public ResponseEntity<List<BookResponse>> findAll() {

        return ResponseEntity.ok(
                bookService.findAll()
        );
    }

    @GetMapping("/search")
    public ResponseEntity<List<BookResponse>> search(
            @RequestParam String keyword
    ) {

        return ResponseEntity.ok(
                bookService.search(keyword)
        );
    }


    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<BookResponse>> findByAuthor(
            @PathVariable Long authorId
    ) {

        return ResponseEntity.ok(
                bookService.findByAuthor(authorId)
        );
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<BookResponse>> findByCategory(
            @PathVariable Long categoryId
    ) {

        return ResponseEntity.ok(
                bookService.findByCategory(categoryId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> findById(
            @PathVariable Long id,
            Authentication authentication
    ) {

        Long borrowerId =
                CurrentUser.getUserId(authentication);

        return ResponseEntity.ok(
                bookService.findById(
                        id,
                        borrowerId
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody BookRequest request
    ) {

        return ResponseEntity.ok(
                bookService.update(id, request)
        );
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookResponse> updateMultipart(
            @PathVariable Long id,
            @Valid @RequestPart("book") BookRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        return ResponseEntity.ok(bookService.update(id, request, images));
    }

    @PutMapping(value = "/{id}/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookResponse> updateWithImages(
            @PathVariable Long id,
            @Valid @RequestPart("book") BookRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        return ResponseEntity.ok(
                bookService.update(id, request, images)
        );
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<BookImageResponse>> addImages(
            @PathVariable Long id,
            @RequestPart("images") List<MultipartFile> images
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookImageService.addImages(id, images));
    }

    @PutMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<BookImageResponse>> replaceImages(
            @PathVariable Long id,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        return ResponseEntity.ok(bookImageService.replaceImages(id, images));
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<List<BookImageResponse>> getImages(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(bookImageService.findImages(id));
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long id,
            @PathVariable Long imageId
    ) {
        bookImageService.deleteImage(id, imageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/images/{imageId}")
    public ResponseEntity<Resource> getImage(
            @PathVariable Long id,
            @PathVariable Long imageId
    ) {
        BookImageService.StoredImage image = bookImageService.getImage(id, imageId);

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(image.contentType());
        } catch (IllegalArgumentException ex) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(image.resource());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {

        bookService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/borrow")
    public ResponseEntity<BorrowingResponse> borrow(
            @PathVariable Long id,
            Authentication authentication
    ) {

        Long borrowerId =
                CurrentUser.getUserId(authentication);

        return ResponseEntity.ok(
                bookService.borrow(id, borrowerId)
        );
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<BorrowingResponse> returnBook(
            @PathVariable Long id,
            Authentication authentication
    ) {

        Long borrowerId =
                CurrentUser.getUserId(authentication);

        return ResponseEntity.ok(
                bookService.returnBook(id, borrowerId)
        );
    }
}
