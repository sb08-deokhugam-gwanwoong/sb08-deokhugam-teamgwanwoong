package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookDto;
import org.springframework.web.multipart.MultipartFile;

public interface BookService {

  BookDto createBook(BookCreateRequest request, MultipartFile thumbnailImage);
}
