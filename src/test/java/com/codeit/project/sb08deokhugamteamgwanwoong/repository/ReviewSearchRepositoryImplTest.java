package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewDocument;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewSearchCondition;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.impl.ReviewSearchRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
// mock(SearchHits.class) 가짜 객체를 대입했기 때문에 경고를 무시해주는 어노테이션
@SuppressWarnings("unchecked")
public class ReviewSearchRepositoryImplTest {

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @InjectMocks
    private ReviewSearchRepositoryImpl reviewSearchRepository;

    @Test
    @DisplayName("조건이 없는 기본 검색 시 예외 없이 search 메서드를 호출한다.")
    void search_by_cursor_default_condition() {
        //given
        ReviewSearchCondition condition = ReviewSearchCondition.builder()
                .orderBy("createdAt")
                .direction(Sort.Direction.DESC)
                .build();
        Pageable pageable = PageRequest.of(0, 20);

        SearchHits<ReviewDocument> mockSearchHits = mock(SearchHits.class);
        given(elasticsearchOperations.search(any(Query.class), eq(ReviewDocument.class))).willReturn(mockSearchHits);

        //when
        reviewSearchRepository.searchByCursor(condition, pageable);

        //then
        then(elasticsearchOperations).should(times(1)).search(any(Query.class), eq(ReviewDocument.class));
    }

    @Test
    @DisplayName("키워드 길이가 1인 경우 ngram 필드만 사용하여 검색한다.")
    void search_by_cursor_keyword_length_1() {
        //given
        ReviewSearchCondition condition = ReviewSearchCondition.builder()
                .keyword("가")
                .orderBy("createdAt")
                .direction(Sort.Direction.DESC)
                .build();
        Pageable pageable = PageRequest.of(0, 20);

        given(elasticsearchOperations.search(any(Query.class), eq(ReviewDocument.class))).willReturn(mock(SearchHits.class));

        //when
        reviewSearchRepository.searchByCursor(condition, pageable);

        //then
        then(elasticsearchOperations).should(times(1)).search(any(Query.class), eq(ReviewDocument.class));
    }

    @Test
    @DisplayName("키워드 길이가 2이상이며 특정 책, 유저 ID 필터가 있을 경우 검색한다.")
    void search_by_cursor_keyword_length_over_2_with_filters() {
        //given
        ReviewSearchCondition condition = ReviewSearchCondition.builder()
                .keyword("스프링")
                .bookId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .orderBy("createdAt")
                .direction(Sort.Direction.DESC)
                .build();
        Pageable pageable = PageRequest.of(0, 20);

        //when
        reviewSearchRepository.searchByCursor(condition, pageable);

        //then
        then(elasticsearchOperations).should(times(1)).search(any(Query.class), eq(ReviewDocument.class));
    }

    @Test
    @DisplayName("평점 정렬 및 커서가 존재할 경우 SearchAfter를 추가하여 검색한다.")
    void search_by_cursor_rating_sort_with_search_after() {
        //given
        ReviewSearchCondition condition = ReviewSearchCondition.builder()
                .orderBy("rating")
                .direction(Sort.Direction.DESC)
                .cursor("5")
                .after(Instant.now())
                .build();
        Pageable pageable = PageRequest.of(0, 20);

        given(elasticsearchOperations.search(any(Query.class), eq(ReviewDocument.class))).willReturn(mock(SearchHits.class));

        //when
        reviewSearchRepository.searchByCursor(condition, pageable);

        //then
        then(elasticsearchOperations).should(times(1)).search(any(Query.class), eq(ReviewDocument.class));
    }

    @Test
    @DisplayName("최신순 정렬 및 커서가 존재할 경우 SearchAfter를 추가하여 검색한다.")
    void search_by_cursor_createdAt_sort_with_search_after() {
        // given
        ReviewSearchCondition condition = ReviewSearchCondition.builder()
                .orderBy("createdAt")
                .direction(Sort.Direction.DESC)
                .cursor("이전 커서 값")
                .after(Instant.now())
                .build();
        Pageable pageable = PageRequest.of(0, 20);

        given(elasticsearchOperations.search(any(Query.class), eq(ReviewDocument.class))).willReturn(mock(SearchHits.class));

        //when
        reviewSearchRepository.searchByCursor(condition, pageable);

        //then
        then(elasticsearchOperations).should(times(1)).search(any(Query.class), eq(ReviewDocument.class));
    }


}
