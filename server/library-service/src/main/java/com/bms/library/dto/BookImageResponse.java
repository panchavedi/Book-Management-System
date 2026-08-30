package com.bms.library.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookImageResponse {

    private Long id;
    private String url;
    private String originalFileName;
    private String contentType;
    private long fileSize;
    private int displayOrder;
}
