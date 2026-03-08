package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {

  boolean existsByIsbn(String isbn);

  // soft-delete된 도서까지 포함해서 조회
  @Query(value = "SELECT * FROM books WHERE id = :id", nativeQuery = true)
  Optional<Book> findByIdIncludeDeleted(@Param("id") UUID id);

  // 물리 삭제
  @Modifying(clearAutomatically = true)
  @Query(value = "DELETE FROM books WHERE id = :id", nativeQuery = true)
  void hardDeleteById(@Param("id") UUID id);
}
