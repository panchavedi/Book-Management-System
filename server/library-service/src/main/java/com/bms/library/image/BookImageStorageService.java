package com.bms.library.image;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.io.IOException;

public interface BookImageStorageService {

    StoredBookImage store(Long bookId, MultipartFile file) throws IOException;

    void delete(String storageKey);

    Resource load(String storageKey);

    record StoredBookImage(
            String storageKey,
            String contentType,
            long fileSize
    ) {}
}
