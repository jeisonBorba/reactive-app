package com.learnreactiveprogramming.service;


import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class FluxAndMonoGeneratorService {

    public Flux<String> namesFlux() {
        return Flux.fromIterable(Arrays.asList("Jeison", "Daniela", "Joice"))
                .log();
    }

    public Flux<String> namesFluxMap(int stringLength) {
        return Flux.fromIterable(Arrays.asList("Jeison", "Daniela", "Joice"))
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .map(s -> s.length() + "-" + s)
                .log();
    }

    public Flux<String> namesFluxFlatMap(int stringLength) {
        return Flux.fromIterable(Arrays.asList("Jeison", "Daniela", "Joice"))
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMap(this::splitString)
                .log();
    }

    public Flux<String> namesFluxFlatMapAsync(int stringLength) {
        return Flux.fromIterable(Arrays.asList("Jeison", "Daniela", "Joice"))
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMap(FluxAndMonoGeneratorService::splitStringWithDelay)
                .log();
    }

    public Flux<String> namesFluxFlatConcatMap(int stringLength) {
        return Flux.fromIterable(Arrays.asList("Jeison", "Daniela", "Joice"))
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .concatMap(FluxAndMonoGeneratorService::splitStringWithDelay)
                .log();
    }

    public Flux<String> namesFluxTransform(int stringLength) {
        Function<Flux<String>, Flux<String>> filterMap = name -> name
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength);

        return Flux.fromIterable(Arrays.asList("Jeison", "Daniela", "Joice"))
                .transform(filterMap)
                .flatMap(this::splitString)
                .defaultIfEmpty("Default")
                .log();
    }

    public Flux<String> namesFluxTransformSwitchIfEmpty(int stringLength) {
        Function<Flux<String>, Flux<String>> filterMap = name -> name
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMap(s -> splitString(s));

        final Flux<String> defaultFlux = Flux.just("DefaultValue")
                .transform(filterMap);

        return Flux.fromIterable(Arrays.asList("Jeison", "Daniela", "Joice"))
                .transform(filterMap)
                .switchIfEmpty(defaultFlux)
                .log();
    }

    public Flux<String> exploreConcatFlux() {
        Flux<String> fluxABC = Flux.just("A", "B", "C");
        Flux<String> fluxDEF = Flux.just("D", "E", "F");
        return Flux.concat(fluxABC, fluxDEF)
                .log();
    }

    public Flux<String> exploreConcatWithFlux() {
        Flux<String> fluxABC = Flux.just("A", "B", "C");
        Flux<String> fluxDEF = Flux.just("D", "E", "F");
        return fluxABC.concatWith(fluxDEF)
                .log();
    }

    public Flux<String> exploreConcatWithMono() {
        Mono<String> monoABC = Mono.just("A");
        Mono<String> monoDEF = Mono.just("D");
        return monoABC.concatWith(monoDEF)
                .log();
    }

    public Flux<String> exploreMergeFlux() {
        Flux<String> fluxABC = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));

        Flux<String> fluxDEF = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(125));

        return Flux.merge(fluxABC, fluxDEF)
                .log();
    }

    public Flux<String> exploreMergeWithFlux() {
        Flux<String> fluxABC = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));

        Flux<String> fluxDEF = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(125));

        return fluxABC.mergeWith(fluxDEF)
                .log();
    }

    public Flux<String> exploreMergeWithMono() {
        Mono<String> fluxABC = Mono.just("A");
        Mono<String> fluxDEF = Mono.just("B");

        return fluxABC.mergeWith(fluxDEF)
                .log();
    }

    public Flux<String> exploreMergeSequentialFlux() {
        Flux<String> fluxABC = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));

        Flux<String> fluxDEF = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(125));

        return Flux.mergeSequential(fluxABC, fluxDEF)
                .log();
    }

    public Flux<String> exploreMergeZipSimpleFlux() {
        Flux<String> fluxABC = Flux.just("A", "B", "C");
        Flux<String> fluxDEF = Flux.just("D", "E", "F");

        return Flux.zip(fluxABC, fluxDEF, (first, second) -> first + second)
                .log();
    }

    public Flux<String> exploreMergeZipFlux() {
        Flux<String> fluxABC = Flux.just("A", "B", "C");
        Flux<String> fluxDEF = Flux.just("D", "E", "F");
        Flux<String> flux123 = Flux.just("1", "2", "3");
        Flux<String> flux456 = Flux.just("4", "5", "6");

        return Flux.zip(fluxABC, fluxDEF, flux123, flux456)
                .map(t4 -> t4.getT1() + t4.getT2() + t4.getT3() + t4.getT4())
                .log();
    }

    public Flux<String> exploreMergeZipWithFlux() {
        Flux<String> fluxABC = Flux.just("A", "B", "C");
        Flux<String> fluxDEF = Flux.just("D", "E", "F");

        return fluxABC.zipWith(fluxDEF, (first, second) -> first + second)
                .log();
    }

    public Mono<String> exploreMergeZipWithMono() {
        Mono<String> monoABC = Mono.just("A");
        Mono<String> monofDEF = Mono.just("B");

        return monoABC.zipWith(monofDEF)
                .map(t2 -> t2.getT1() + t2.getT2())
                .log();
    }

    private Flux<String> splitString(String name) {
        String[] charArray = name.split("");
        return Flux.fromArray(charArray);
    }

    private static Flux<String> splitStringWithDelay(String name) {
        String[] charArray = name.split("");
        int delay = new Random().nextInt(1000);
        return Flux.fromArray(charArray)
                .delayElements(Duration.ofMillis(delay));
    }

    public Flux<String> namesFluxImmutability() {
        Flux<String> namesFlux = Flux.fromIterable(Arrays.asList("Jeison", "Daniela", "Joice"));
        Flux<String> namesFluxMap = namesFlux.map(String::toUpperCase);
        return namesFluxMap;
    }

    public Mono<String> namesMono(int stringLength) {
        return Mono.just("Jeison")
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .log();
    }

    public Mono<List<String>> namesMonoFlatMap(int stringLength) {
        return Mono.just("Jeison")
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMap(this::splitStringMono)
                .log();
    }

    public Flux<String> namesMonoFlatMapMany(int stringLength) {
        return Mono.just("Jeison")
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMapMany(this::splitString)
                .log();
    }

    private Mono<List<String>> splitStringMono(String name) {
        String[] splited = name.split("");
        return Mono.just(Arrays.asList(splited));
    }

    public static void main(String[] args) {
        FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();
        fluxAndMonoGeneratorService.namesFlux()
                .subscribe(name -> System.out.println("Flux name is: " + name));

        fluxAndMonoGeneratorService.namesMono(5)
                .subscribe(name -> System.out.println("Mono name is: " + name));
    }
}
