package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import com.reactivespring.repository.ReviewReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ReviewHandler {
    private Sinks.Many<Review> reviewsSink = Sinks.many().replay().latest();

    final private ReviewReactiveRepository reviewReactiveRepository;
    final private Validator validator;

    public ReviewHandler(ReviewReactiveRepository reviewReactiveRepository, Validator validator) {
        this.reviewReactiveRepository = reviewReactiveRepository;
        this.validator = validator;
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(Review.class)
                .doOnNext(this::validate)
                .flatMap(reviewReactiveRepository::save)
                .doOnNext(review -> {
                    reviewsSink.tryEmitNext(review);
                })
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
    }

    public Mono<ServerResponse> getReviews(ServerRequest request) {
        Optional<String> moveInfoId = request.queryParam("movieInfoId");
        if (moveInfoId.isPresent()) {
            Flux<Review> reviewsByMovieInfoId = reviewReactiveRepository.findReviewsByMovieInfoId(Long.valueOf(moveInfoId.get()));
            return buildReviewsResponse(reviewsByMovieInfoId);
        } else {
            Flux<Review> reviewsFlux = reviewReactiveRepository.findAll();
            return buildReviewsResponse(reviewsFlux);
        }
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {
        String reviewId = request.pathVariable("id");
        Mono<Review> foundReview = reviewReactiveRepository.findById(reviewId)
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not found for the given id "+ reviewId)));

        return foundReview
                .flatMap(review -> request.bodyToMono(Review.class)
                        .map(reqReview -> {
                            review.setComment(reqReview.getComment());
                            review.setRating(reqReview.getRating());
                            return review;
                        })
                        .flatMap(reviewReactiveRepository::save)
                        .flatMap(ServerResponse.status(HttpStatus.OK)::bodyValue)
                );
    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {
        String reviewId = request.pathVariable("id");
        Mono<Review> foundReview = reviewReactiveRepository.findById(reviewId);

        return foundReview
                .flatMap(review -> reviewReactiveRepository.deleteById(reviewId))
                .then(ServerResponse.status(HttpStatus.NO_CONTENT).build());
    }

    private Mono<ServerResponse> buildReviewsResponse(Flux<Review> reviewsFlux) {
        return ServerResponse.status(HttpStatus.OK).body(reviewsFlux, Review.class);
    }

    private void validate(Review review) {
        Set<ConstraintViolation<Review>> constraintVaiolations = validator.validate(review);
        log.info("constraintVaiolations: {}", constraintVaiolations);

        if (constraintVaiolations.size() > 0) {
            String errorMessage = constraintVaiolations.stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(","));
            throw new ReviewDataException(errorMessage);
        }
    }

    public Mono<ServerResponse> getReviewsAsStream(ServerRequest request) {
        return ServerResponse.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_NDJSON)
                .body(reviewsSink.asFlux(), Review.class)
                .log();
    }
}
