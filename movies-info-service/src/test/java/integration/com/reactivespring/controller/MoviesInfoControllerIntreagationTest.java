package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class MoviesInfoControllerIntreagationTest {
    private static final String MOVIES_INFO_URL = "/v1/movieinfos";

    @Autowired
    private MovieInfoRepository movieInfoRepository;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        List<MovieInfo> movieInfos = Arrays.asList(
                new MovieInfo(null, "Batman Begins", 2005, Arrays.asList("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight", 2008, Arrays.asList("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises", 2012, Arrays.asList("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"))
        );

        movieInfoRepository.saveAll(movieInfos).blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void getAllMovieInfos() {
        webTestClient
                .get()
                .uri(MOVIES_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getAllMovieInfos_stream() {
        MovieInfo movieInfo = new MovieInfo(null, "Batman Begins Mock", 2005, Arrays.asList("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    MovieInfo savedMoviInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(savedMoviInfo);
                    assertNotNull(savedMoviInfo.getMovieInfoId());
                });

        Flux<MovieInfo> movieStreamFlux = webTestClient
                .get()
                .uri(MOVIES_INFO_URL+"/stream")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(MovieInfo.class)
                .getResponseBody();

        StepVerifier.create(movieStreamFlux)
                .assertNext(Assertions::assertNotNull)
                .thenCancel()
                .verify();
    }

    @Test
    void getMovieInfosByYear() {
        URI uri = UriComponentsBuilder
                .fromUriString(MOVIES_INFO_URL)
                .queryParam("year", 2005)
                .buildAndExpand()
                .toUri();

        webTestClient
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);
    }

    @Test
    void getMovieInfosById() {
        String movieInfoId = "abc";

        webTestClient
                .get()
                .uri(MOVIES_INFO_URL+"/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    MovieInfo foundMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(foundMovieInfo);
                    assertEquals(movieInfoId, foundMovieInfo.getMovieInfoId());
                    assertEquals("Dark Knight Rises", foundMovieInfo.getName());
                });
    }

    @Test
    void getMovieInfosByIdNotFound() {
        String movieInfoId = "def";

        webTestClient
                .get()
                .uri(MOVIES_INFO_URL+"/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void getMovieInfosByIdWithJsonPath() {
        String movieInfoId = "abc";

        webTestClient
                .get()
                .uri(MOVIES_INFO_URL+"/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Dark Knight Rises");
    }

    @Test
    void addMovieInfo() {
        MovieInfo movieInfo = new MovieInfo(null, "Batman Begins Mock", 2005, Arrays.asList("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    MovieInfo savedMoviInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(savedMoviInfo);
                    assertNotNull(savedMoviInfo.getMovieInfoId());
                });
    }

    @Test
    void updateMovieInfo() {
        String movieInfoId = "abc";
        MovieInfo movieInfo = new MovieInfo(null, "Batman Begins Mock", 2005, Arrays.asList("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        webTestClient
                .put()
                .uri(MOVIES_INFO_URL+"/{id}", movieInfoId)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    MovieInfo updatedMoviInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(updatedMoviInfo);
                    assertEquals("Batman Begins Mock", updatedMoviInfo.getName());
                });
    }

    @Test
    void updateMovieInfoNotFound() {
        String movieInfoId = "def";
        MovieInfo movieInfo = new MovieInfo(null, "Batman Begins Mock", 2005, Arrays.asList("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        webTestClient
                .put()
                .uri(MOVIES_INFO_URL+"/{id}", movieInfoId)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void deleteMovieInfo() {
        String movieInfoId = "abc";

        webTestClient
                .delete()
                .uri(MOVIES_INFO_URL+"/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .isNoContent();

        webTestClient
                .get()
                .uri(MOVIES_INFO_URL)
                .exchange()
                .expectBodyList(MovieInfo.class)
                .hasSize(2);
    }
}