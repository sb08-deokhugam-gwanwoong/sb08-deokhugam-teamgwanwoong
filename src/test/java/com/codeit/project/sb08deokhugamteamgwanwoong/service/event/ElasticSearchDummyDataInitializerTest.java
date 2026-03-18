package com.codeit.project.sb08deokhugamteamgwanwoong.service.event;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.event.ElasticSearchDummyDataInitializer;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.ReviewRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.ReviewSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class ElasticSearchDummyDataInitializerTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ReviewSearchRepository reviewSearchRepository;

    @InjectMocks
    private ElasticSearchDummyDataInitializer initializer;

    private Review review;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .email("test@codeit.com")
                .nickname("testUser")
                .password("password01!")
                .build();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

        Book book = Book.builder()
                .title("testBook")
                .author("testAuthor")
                .isbn("9788994492032")
                .publisher("testPublisher")
                .publishedDate(LocalDate.now())
                .description("testDescription")
                .thumbnailUrl("https://test-thumbnail.url/image.jpg")
                .build();
        ReflectionTestUtils.setField(book, "id", UUID.randomUUID());

        review = Review.builder()
                .rating(5)
                .content("재밌어요")
                .user(user)
                .book(book)
                .build();
        ReflectionTestUtils.setField(review, "id", UUID.randomUUID());
    }

    @Test
    @DisplayName("더미 데이터 동기화 - DB에 데이터가 있는 경우 변환 후 ES에 저장한다.")
    void run_initializer() {
        //given
        Page<Review> reviewPage = new PageImpl<>(List.of(review));
        given(reviewRepository.findAll(any(Pageable.class))).willReturn(reviewPage);

        Thread.currentThread().interrupt();

        //when
        initializer.syncDummyDataToElasticSearch();

        //then
        then(reviewSearchRepository).should(times(1)).deleteAll();
        then(reviewRepository).should(times(1)).findAll(any(Pageable.class));
        then(reviewSearchRepository).should(times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("더미 데이터 동기화 - DB에 데이터가 없는 경우 ES에 저장하지 않는다.")
    void sync_dummy_data_empty() {
        //given
        Page<Review> emptyPage = new PageImpl<>(Collections.emptyList());
        given(reviewRepository.findAll(any(Pageable.class))).willReturn(emptyPage);

        Thread.currentThread().interrupt();

        initializer.syncDummyDataToElasticSearch();

        then(reviewSearchRepository).should(times(1)).deleteAll();
        then(reviewRepository).should(times(1)).findAll(any(Pageable.class));
        then(reviewSearchRepository).should(never()).saveAll(anyList());
    }
}
