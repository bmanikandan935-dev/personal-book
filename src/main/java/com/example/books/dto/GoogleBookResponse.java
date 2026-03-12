package com.example.books.dto;

import java.util.List;

public record GoogleBookResponse(
        String id,
        VolumeInfo volumeInfo
) {
    public record VolumeInfo(
            String title,
            List<String> authors,
            Integer pageCount
    ) {}
}
