package com.codeit.project.sb08deokhugamteamgwanwoong.event;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewDocument;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.event.ReviewCreatedEvent;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.event.ReviewDeletedEvent;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.event.ReviewUpdatedEvent;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.ReviewSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewSyncEventListener {

    private final ReviewSearchRepository reviewSearchRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReviewCreated(ReviewCreatedEvent event) {
        log.info("ES Sync: 리뷰 생성 이벤트 수신 - ReviewId: {}", event.review().getId());
        ReviewDocument document = convertToDocument(event.review());
        reviewSearchRepository.save(document);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReviewUpdated(ReviewUpdatedEvent event) {
        log.info("ES Sync: 리뷰 수정 이벤트 수신 - ReviewId: {}", event.review().getId());
        ReviewDocument document = convertToDocument(event.review());
        reviewSearchRepository.save(document);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReviewDeleted(ReviewDeletedEvent event) {
        UUID reviewId = event.reviewId();
        log.info("ES Sync: 리뷰 삭제 이벤트 수신 - ReviewId: {}", reviewId);
        reviewSearchRepository.deleteById(reviewId.toString());
    }

    private ReviewDocument convertToDocument(Review review) {
        return ReviewDocument.builder()
                .review(review)
                .build();
    }
}
