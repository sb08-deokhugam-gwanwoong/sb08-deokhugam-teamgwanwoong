package com.codeit.project.sb08deokhugamteamgwanwoong.config;

import com.codeit.project.sb08deokhugamteamgwanwoong.repository.BookRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.CommentRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.NotificationRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.ReviewLikeRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.ReviewRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.UserRepository;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MetricsConfig {

  private final UserRepository userRepository;
  private final BookRepository bookRepository;
  private final ReviewRepository reviewRepository;
  private final CommentRepository commentRepository;
  private final NotificationRepository notificationRepository;
  private final ReviewLikeRepository reviewLikeRepository;

  @Bean
  public MeterBinder businessMetricsBinder() {
    return registry -> {
      // 총 가입자 수
      registry.gauge("deokhugam.users.total", userRepository, repo -> repo.count());

      // 총 도서 권수
      registry.gauge("deokhugam.books.total", bookRepository, repo -> repo.count());

      // 총 리뷰 수
      registry.gauge("deokhugam.reviews.total", reviewRepository, repo -> repo.count());

      // 총 댓글 수
      registry.gauge("deokhugam.comments.total", commentRepository, repo -> repo.count());

      // 총 알림 수
      registry.gauge("deokhugam.notifications.total", notificationRepository, repo -> repo.count());

      // 총 리뷰 좋아요 수
      registry.gauge("deokhugam.review_likes.total", reviewLikeRepository, repo -> repo.count());
    };
  }
}
