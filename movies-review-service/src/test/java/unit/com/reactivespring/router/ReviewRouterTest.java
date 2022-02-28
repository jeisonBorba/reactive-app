package com.reactivespring.router;

import com.reactivespring.domain.Review;
import com.reactivespring.exceptionhandler.GlobalErrorHandler;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactiveRepository;
//import com.reactivespring.validator.ReviewValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, GlobalErrorHandler.class})
@AutoConfigureWebTestClient
public class ReviewRouterTest {
    private static final String REVIEWS_URL = "/v1/reviews";

    @MockBean
    private ReviewReactiveRepository reviewReactiveRepository;

//    @MockBean
//    private ReviewValidator reviewValidator;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void getAllReviews() {
        List<Review> reviewList = Arrays.asList(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0));

        when(reviewReactiveRepository.findAll()).thenReturn(Flux.fromIterable(reviewList));
//        doCallRealMethod().when(reviewValidator).validate(any(), any());

        webTestClient
                .get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .value(reviews -> {
                    assertEquals(3, reviews.size());
                });
    }

    @Test
    void addReview() {
        Review review = new Review(null, 1L, "Awesome Movie", 9.0);

        when(reviewReactiveRepository.save(isA(Review.class))).thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));
//        doCallRealMethod().when(reviewValidator).validate(any(), any());

        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Review.class)
                .consumeWith(reviewResponse ->{
                    Review savedReview = reviewResponse.getResponseBody();

                    assertNotNull(savedReview);
                    assertNotNull(savedReview.getReviewId());
                    assertEquals("abc", savedReview.getReviewId());

                });

    }

    @Test
    void addReviewValidations() {
        Review review = new Review(null, null, "Awesome Movie", -9.0);

        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .isEqualTo("review.movieInfoId: must not be null,review.rating: please pass a non-negative value");
    }

    @Test
    void updateReview() {
        Review reviewUpdate = new Review(null, 1L, "Not an Awesome Movie", 8.0);

        when(reviewReactiveRepository.save(isA(Review.class))).thenReturn(Mono.just(new Review("abc", 1L, "Not an Awesome Movie", 8.0)));
        when(reviewReactiveRepository.findById((String) any())).thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));
//        doCallRealMethod().when(reviewValidator).validate(any(), any());

        webTestClient
                .put()
                .uri(REVIEWS_URL+"/{id}", "abc")
                .bodyValue(reviewUpdate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Review.class)
                .consumeWith(reviewResponse ->{
                    Review updatedReview = reviewResponse.getResponseBody();

                    assertNotNull(updatedReview);
                    assertEquals(8.0,updatedReview.getRating());
                    assertEquals("Not an Awesome Movie", updatedReview.getComment());
                });
    }

    @Test
    void deleteReview() {
        String reviewId= "abc";
        when(reviewReactiveRepository.findById((String) any())).thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));
        when(reviewReactiveRepository.deleteById((String) any())).thenReturn(Mono.empty());
//        doCallRealMethod().when(reviewValidator).validate(any(), any());

        webTestClient
                .delete()
                .uri(REVIEWS_URL+"/{id}", reviewId)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

}