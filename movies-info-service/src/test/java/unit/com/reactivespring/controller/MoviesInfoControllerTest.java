package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MoviesInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = MoviesInfoController.class)
@AutoConfigureWebTestClient
class MoviesInfoControllerTest {
    private static final String MOVIES_INFO_URL = "/v1/movieinfos";

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MoviesInfoService moviesInfoServiceMock;

    @Test
    void getAllMovieInfos() {
        List<MovieInfo> movieInfos = Arrays.asList(
                new MovieInfo(null, "Batman Begins", 2005, Arrays.asList("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight", 2008, Arrays.asList("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises", 2012, Arrays.asList("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"))
        );
        when(moviesInfoServiceMock.getAllMovieInfos()).thenReturn(Flux.fromIterable(movieInfos));

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
    void getMovieInfosById() {
        String movieInfoId = "abc";
        MovieInfo movieInfo = new MovieInfo("abc", "Dark Knight Rises", 2012, Arrays.asList("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        when(moviesInfoServiceMock.getAllMovieInfosById(movieInfoId)).thenReturn(Mono.just(movieInfo));

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
        MovieInfo movieInfo = new MovieInfo("mockId", "Batman Begins Mock", 2005, Arrays.asList("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        when(moviesInfoServiceMock.addMovieInfo(isA(MovieInfo.class))).thenReturn(Mono.just(movieInfo));

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
                    assertEquals("mockId", savedMoviInfo.getMovieInfoId());
                });
    }

    @Test
    void addMovieInfoValidation() {
        MovieInfo movieInfo = new MovieInfo("mockId", "", -2005, Collections.singletonList(""), LocalDate.parse("2005-06-15"));

        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    String responseBody = stringEntityExchangeResult.getResponseBody();
                    assertNotNull(responseBody);
                    String expectedErrorMessage = "movieInfo.cast must be present,movieInfo.name must be present,movieInfo.year must be a positive value";
                    assertEquals(expectedErrorMessage, responseBody);
                });
    }

    @Test
    void updateMovieInfo() {
        String movieInfoId = "abc";
        MovieInfo movieInfo = new MovieInfo(null, "Batman Begins", 2005, Arrays.asList("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
        MovieInfo movieInfoMock = new MovieInfo(movieInfoId, "Dark Knight Rises Mock", 2005, Arrays.asList("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        when(moviesInfoServiceMock.updateMovieInfo(isA(MovieInfo.class), isA(String.class))).thenReturn(Mono.just(movieInfoMock));

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
                    assertEquals("Dark Knight Rises Mock", updatedMoviInfo.getName());
                });
    }

    @Test
    void deleteMovieInfo() {
        String movieInfoId = "abc";

        when(moviesInfoServiceMock.deleteMovieInfo(isA(String.class))).thenReturn(Mono.empty());

        webTestClient
                .delete()
                .uri(MOVIES_INFO_URL+"/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .isNoContent();
    }
}