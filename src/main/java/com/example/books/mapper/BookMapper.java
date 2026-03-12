package com.example.books.mapper;

import com.example.books.dto.GoogleBookResponse;
import com.example.books.entity.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookMapper {

    @Mapping(source = "id", target = "googleId")
    @Mapping(source = "volumeInfo.title", target = "title")
    @Mapping(source = "volumeInfo.authors", target = "author", qualifiedByName = "firstAuthor")
    @Mapping(source = "volumeInfo.pageCount", target = "pageCount")
    Book toBook(GoogleBookResponse response);

    @Named("firstAuthor")
    default String firstAuthor(List<String> authors) {
        if (authors == null || authors.isEmpty()) {
            return null;
        }
        return authors.get(0);
    }
}
