package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    @Query("SELECT r From Review r JOIN FETCH r.user JOIN FETCH r.book")
    List<Review> findAll();

    @Query("SELECT r FROM Review r JOIN FETCH r.user JOIN FETCH r.book WHERE r.book.id = :bookId")
    List<Review> findAllByBookId(@Param("bookId") UUID bookId);

    boolean existsByBookIdAndUserId(UUID bookId, UUID userId);

    @Query(value = "SELECT * FROM reviews WHERE id = :id", nativeQuery = true)
    Optional<Review> findByIdIncludeDeleted(@Param("id") UUID id);

    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM reviews WHERE id = :id", nativeQuery = true)
    void hardDeleteById(@Param("id") UUID id);
}
