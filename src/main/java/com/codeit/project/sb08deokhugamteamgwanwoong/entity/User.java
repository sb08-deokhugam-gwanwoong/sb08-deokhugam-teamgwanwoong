package com.codeit.project.sb08deokhugamteamgwanwoong.entity;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.base.BaseRemovableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL") // 조회 시 논리 삭제되지 않은 데이터만 보여줌
public class User extends BaseRemovableEntity {

  @Column(nullable = false, unique = true, length = 50)
  private String email;

  @Column(nullable = false, unique = true, length = 50)
  private String nickname;

  @Column(nullable = false, length = 100)
  private String password;

  @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<Review> reviews = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<Comment> comments = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<Notification> notifications = new ArrayList<>();

  @Builder
  public User(String email, String nickname, String password) {
    this.email = email;
    this.nickname = nickname;
    this.password = password;
  }

  /**
   * 닉네임 수정
   * @param nickname 닉네임
   */
  public void updateNickname(String nickname) {
    this.nickname = nickname;
  }
}
