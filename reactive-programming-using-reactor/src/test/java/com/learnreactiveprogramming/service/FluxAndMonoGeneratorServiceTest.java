package com.learnreactiveprogramming.service;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

class FluxAndMonoGeneratorServiceTest {

    FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

    @Test
    public void nameFlux() {
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFlux();

        StepVerifier.create(namesFlux)
                .expectNext("Jeison", "Daniela", "Joice")
                .verifyComplete();

        StepVerifier.create(namesFlux)
                .expectNextCount(3)
                .verifyComplete();

        StepVerifier.create(namesFlux)
                .expectNext("Jeison")
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void namesFluxMap() {
        int stringLength = 5;
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFluxMap(stringLength);
        StepVerifier.create(namesFlux)
                .expectNext("6-JEISON", "7-DANIELA")
                .verifyComplete();
    }

    @Test
    public void namesFluxImmutability() {
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFluxImmutability();
        StepVerifier.create(namesFlux)
                .expectNext("JEISON", "DANIELA", "JOICE")
                .verifyComplete();
    }

    @Test
    public void namesFluxFlatMap() {
        int stringLength = 5;
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFluxFlatMap(stringLength);
        StepVerifier.create(namesFlux)
                .expectNext("J","E","I","S","O","N","D","A","N","I","E","L","A")
                .verifyComplete();
    }

    @Test
    public void namesFluxFlatMapAsync() {
        int stringLength = 5;
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFluxFlatMapAsync(stringLength);
        StepVerifier.create(namesFlux)
                .expectNextCount(13)
                .verifyComplete();
    }

    @Test
    public void namesFluxFlatConcatMap() {
        int stringLength = 5;
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFluxFlatConcatMap(stringLength);
        StepVerifier.create(namesFlux)
                .expectNext("J","E","I","S","O","N","D","A","N","I","E","L","A")
                .verifyComplete();
    }

    @Test
    public void namesFluxTranform() {
        int stringLength = 5;
        Flux<String> namesMono = fluxAndMonoGeneratorService.namesFluxTransform(stringLength);
        StepVerifier.create(namesMono)
                .expectNext("J","E","I","S","O","N","D","A","N","I","E","L","A")
                .verifyComplete();
    }

    @Test
    public void namesFluxTranformDefaultIfEmpty() {
        int stringLength = 8;
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFluxTransform(stringLength);
        StepVerifier.create(namesFlux)
                .expectNext("Default")
                .verifyComplete();
    }

    @Test
    public void namesFluxTranformSwitchIfEmpty() {
        int stringLength = 8;
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFluxTransformSwitchIfEmpty(stringLength);
        StepVerifier.create(namesFlux)
                .expectNext("D","E","F","A","U","L","T","V","A","L","U","E")
                .verifyComplete();
    }

    @Test
    public void namesMonoFlatMap() {
        int stringLength = 5;
        Mono<List<String>> namesMono = fluxAndMonoGeneratorService.namesMonoFlatMap(stringLength);
        StepVerifier.create(namesMono)
                .expectNext(Arrays.asList("J","E","I","S","O","N"))
                .verifyComplete();
    }

    @Test
    public void namesMonoFlatMapMany() {
        int stringLength = 5;
        Flux<String> namesMono = fluxAndMonoGeneratorService.namesMonoFlatMapMany(stringLength);
        StepVerifier.create(namesMono)
                .expectNext("J","E","I","S","O","N")
                .verifyComplete();
    }

    @Test
    public void exploreConcat() {
        Flux<String> namesFlux = fluxAndMonoGeneratorService.exploreConcatFlux();
        StepVerifier.create(namesFlux)
                .expectNext("A","B","C","D","E","F")
                .verifyComplete();
    }

    @Test
    public void exploreMerge() {
        Flux<String> namesFlux = fluxAndMonoGeneratorService.exploreMergeFlux();
        StepVerifier.create(namesFlux)
                .expectNext("A","D","B","E","C","F")
                .verifyComplete();
    }

    @Test
    public void exploreMergeSequential() {
        Flux<String> namesFlux = fluxAndMonoGeneratorService.exploreMergeSequentialFlux();
        StepVerifier.create(namesFlux)
                .expectNext("A","B","C","D","E","F")
                .verifyComplete();
    }

    @Test
    public void exploreSimpleZip() {
        Flux<String> namesFlux = fluxAndMonoGeneratorService.exploreMergeZipSimpleFlux();
        StepVerifier.create(namesFlux)
                .expectNext("AD","BE","CF")
                .verifyComplete();
    }

    @Test
    public void exploreZip() {
        Flux<String> namesFlux = fluxAndMonoGeneratorService.exploreMergeZipFlux();
        StepVerifier.create(namesFlux)
                .expectNext("AD14","BE25","CF36")
                .verifyComplete();
    }

}