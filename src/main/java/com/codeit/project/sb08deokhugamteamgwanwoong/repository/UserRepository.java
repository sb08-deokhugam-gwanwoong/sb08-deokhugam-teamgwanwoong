package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

  boolean existsByEmail(String email);

  Optional<User> findByEmailAndPassword(String email, String password);

  boolean existsByNickname(String nickname);

  @Query(value = "SELECT * FROM users WHERE id = :userId", nativeQuery = true)
  Optional<User> findByIdIncludeDeleted(@Param("userId") UUID userId);

  @Query(value = "SELECT * FROM users WHERE deleted_at <= :deleteAt", nativeQuery = true)
  List<User> findAllExpiredUsers(@Param("deleteAt") Instant deleteAt);
}
