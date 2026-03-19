package com.codeit.project.sb08deokhugamteamgwanwoong.event;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewDocument;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.ReviewRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.ReviewSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticSearchDummyDataInitializer {

    private final ReviewRepository reviewRepository;
    private final ReviewSearchRepository reviewSearchRepository;

    // 스프링 부트가 완전히 실행 준비를 마친 후(ApplicationReadyEvent)에 단 한번 실행
    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void syncDummyDataToElasticSearch() {
        log.info("Service: ElasticSearch 기존 데이터 초기화 및 DB 더미 데이터 동기화 시작합니다.");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // ES에 남아있는 과거 데이터 모두 삭제
        reviewSearchRepository.deleteAll();

        int pageSize = 100;
        int pageNumber = 0;
        Page<Review> reviewPage;

        int totalSavedCount = 0;

        do {
            PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
            reviewPage = reviewRepository.findAll(pageRequest);

            List<ReviewDocument> documentList = reviewPage.stream()
                    .map(review -> ReviewDocument.builder()
                            .review(review)
                            .build())
                    .toList();
            if (!documentList.isEmpty()) {
                reviewSearchRepository.saveAll(documentList);
                totalSavedCount += documentList.size();
                log.info("Service: 현재 {}개의 데이터를 ES에 저장했습니다....", totalSavedCount);
            }

            pageNumber++;
        } while (reviewPage.hasNext());

        log.info("Service: 총 {}개의 최신 리뷰 데이터가 ES에 성공적으로 저장되었습니다.", totalSavedCount);
    }
}
