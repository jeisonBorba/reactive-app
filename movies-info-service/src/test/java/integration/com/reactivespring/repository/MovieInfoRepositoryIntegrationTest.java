package com.reactivespring.repository;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
class MovieInfoRepositoryIntegrationTest {

    @Autowired
    private MovieInfoRepository movieInfoRepository;

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
    public void findAll() {
        Flux<MovieInfo> moviesInfoFlux = movieInfoRepository.findAll().log();

        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    public void findById() {
        Mono<MovieInfo> movieInfoMono = movieInfoRepository.findById("abc").log();

        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo -> {
                    assertEquals("Dark Knight Rises", movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    public void findByName() {
        Mono<MovieInfo> movieInfoMono = movieInfoRepository.findByName("The Dark Knight").log();

        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo -> {
                    assertEquals("The Dark Knight", movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    public void findByYear() {
        Flux<MovieInfo> movieInfoFlux = movieInfoRepository.findByYear(2005).log();

        StepVerifier.create(movieInfoFlux)
                .assertNext(movieInfo -> {
                    assertEquals("Batman Begins", movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    public void saveMovieInfo() {
        MovieInfo newMovieInfo = new MovieInfo(null, "Batman Begins", 2005, Arrays.asList("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
        Mono<MovieInfo> movieInfoMono = movieInfoRepository.save(newMovieInfo).log();

        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo -> {
                    assertNotNull(movieInfo.getMovieInfoId());
                    assertEquals("Batman Begins", movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    public void updateMovieInfo() {
        MovieInfo foundMovieInfo = movieInfoRepository.findById("abc").block();
        foundMovieInfo.setYear(2022);

        Mono<MovieInfo> movieInfoMono = movieInfoRepository.save(foundMovieInfo).log();

        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo -> {
                    assertEquals(2022, movieInfo.getYear());
                })
                .verifyComplete();
    }

    @Test
    public void deleteMovieInfo() {
        movieInfoRepository.deleteById("abc").block();
        Flux<MovieInfo> movieInfosFlux = movieInfoRepository.findAll().log();

        StepVerifier.create(movieInfosFlux)
                .expectNextCount(2)
                .verifyComplete();
    }

}