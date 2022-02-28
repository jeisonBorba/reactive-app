package com.reactivespring.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8084)
@TestPropertySource(
        properties = {
                "restClient.moviesInfoUrl=http://localhost:8084/v1/movieinfos",
                "restClient.reviewsUrl=http://localhost:8084/v1/reviews"
        }
)
public class MoviesControllerIntegrationTest {
    private static final String MOVIEWS_URL = "/v1/movies";

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void retrieveMovieById() {
        String movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos" + "/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")
                ));

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")
                ));

        webTestClient
                .get()
                .uri(MOVIEWS_URL+"/{id}", movieId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    Movie movie = movieEntityExchangeResult.getResponseBody();
                    assertNotNull(movie);
                    assertEquals("Batman Begins", movie.getMovieInfo().getName());
                    assertEquals(2, movie.getReviewList().size());
                });
    }

    @Test
    void retrieveMovieById_404() {
        String movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos" + "/" + movieId))
                .willReturn(aResponse()
                        .withStatus(404)
                ));

        webTestClient
                .get()
                .uri(MOVIEWS_URL+"/{id}", movieId)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(String.class)
                .isEqualTo("There is no MovieInfo available for the passed Id: abc");

        WireMock.verify(1, getRequestedFor(urlEqualTo("/v1/movieinfos" + "/" + movieId)));
    }

    @Test
    void retrieveMovieByIdReviews_404() {
        String movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos" + "/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")
                ));

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withStatus(404)
                ));

        webTestClient
                .get()
                .uri(MOVIEWS_URL+"/{id}", movieId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    Movie movie = movieEntityExchangeResult.getResponseBody();
                    assertNotNull(movie);
                    assertEquals("Batman Begins", movie.getMovieInfo().getName());
                    assertEquals(0, movie.getReviewList().size());
                });
    }

    @Test
    void retrieveMovieById_5XX() {
        String movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos" + "/" + movieId))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("MovieInfo Service Not Available")
                ));

        webTestClient
                .get()
                .uri(MOVIEWS_URL+"/{id}", movieId)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server Exception in MoviesInfoService MovieInfo Service Not Available");

        WireMock.verify(4, getRequestedFor(urlEqualTo("/v1/movieinfos" + "/" + movieId)));
    }

    @Test
    void retrieveReviews_5XX() {
        String movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos" + "/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")
                ));

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Review Service Not Available")
                ));

        webTestClient
                .get()
                .uri(MOVIEWS_URL+"/{id}", movieId)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server Exception in ReviewsService Review Service Not Available");

        WireMock.verify(4, getRequestedFor(urlPathMatching("/v1/reviews*")));
    }
}
