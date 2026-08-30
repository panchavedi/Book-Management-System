package com.bms.library.service;

import com.bms.library.dto.BookImageResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BookImageService {

    List<BookImageResponse> addImages(Long bookId, List<MultipartFile> images);

    List<BookImageResponse> replaceImages(Long bookId, List<MultipartFile> images);

    void deleteImage(Long bookId, Long imageId);

    void scheduleDeleteForBook(Long bookId);

    StoredImage getImage(Long bookId, Long imageId);

    List<BookImageResponse> findImages(Long bookId);

    record StoredImage(Resource resource, String contentType, String fileName) {}
}
