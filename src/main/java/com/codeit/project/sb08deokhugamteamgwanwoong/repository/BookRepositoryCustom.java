package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookSearchCondition;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import java.util.List;
import org.springframework.data.domain.Pageable;

// 동적 쿼리용 커스텀 인터페이스
public interface BookRepositoryCustom {

  // 커서 기반으로 도서 목록을 검색하는 뼈대 메서드
  List<Book> findAllByCursor(BookSearchCondition condition, Pageable pageable);

}