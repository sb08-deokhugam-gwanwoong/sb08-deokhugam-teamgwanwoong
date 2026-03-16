package com.codeit.project.sb08deokhugamteamgwanwoong.config;

import com.codeit.project.sb08deokhugamteamgwanwoong.repository.BookRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.CommentRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.NotificationRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.ReviewLikeRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.ReviewRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MetricsConfig {

    private final MeterRegistry meterRegistry;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;
    private final CommentRepository commentRepository;
    private final NotificationRepository notificationRepository;
    private final ReviewLikeRepository reviewLikeRepository;

    @PostConstruct
    public void registerBusinessMetrics() {

        //총 가입자 수
        meterRegistry.gauge("deokhugam.users.total", userRepository, UserRepository::count);

        //총 도서 권수
        meterRegistry.gauge("deokhugam.books.total", bookRepository, BookRepository::count);

        //총 리뷰 수
        meterRegistry.gauge("deokhugam.reviews.total", reviewRepository, ReviewRepository::count);

        //총 댓글 수
        meterRegistry.gauge("deokhugam.comments.total", commentRepository, CommentRepository::count);

        //총 알림 수
        meterRegistry.gauge("deokhugam.notifications.total", notificationRepository, NotificationRepository::count);

        //총 리뷰 좋아요 수
        meterRegistry.gauge("deokhugam.review_likes.total", reviewLikeRepository, ReviewLikeRepository::count);
    }
}
