package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookUpdateRequest;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface BookService {

  BookDto createBook(BookCreateRequest request, MultipartFile thumbnailImage);
  BookDto getBook(UUID bookId);
  BookDto updateBook(UUID bookId, BookUpdateRequest request, MultipartFile thumbnailImage);
  void softDeleteBook(UUID bookId);
  void hardDeleteBook(UUID bookId);
}
