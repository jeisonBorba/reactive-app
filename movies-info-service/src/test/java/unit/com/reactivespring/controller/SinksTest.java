package com.reactivespring.controller;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class SinksTest {

    @Test
    void sink() {
        Sinks.Many<Integer> replacySink = Sinks.many().replay().all();

        replacySink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        replacySink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        Flux<Integer> integerFlux = replacySink.asFlux();
        integerFlux.subscribe(integer -> {
            System.out.println("Subscriber 1: "+ integer);
        });

        Flux<Integer> integerFlux1 = replacySink.asFlux();
        integerFlux1.subscribe(integer -> {
            System.out.println("Subscriber 2: "+ integer);
        });

        replacySink.tryEmitNext(3);

        Flux<Integer> integerFlux2 = replacySink.asFlux();
        integerFlux2.subscribe(integer -> {
            System.out.println("Subscriber 3: "+ integer);
        });
    }

    @Test
    void sink_multicast() {
        Sinks.Many<Integer> multicast = Sinks.many().multicast().onBackpressureBuffer();

        multicast.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        multicast.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        Flux<Integer> integerFlux = multicast.asFlux();
        integerFlux.subscribe(integer -> {
            System.out.println("Subscriber 1: "+ integer);
        });

        Flux<Integer> integerFlux1 = multicast.asFlux();
        integerFlux1.subscribe(integer -> {
            System.out.println("Subscriber 2: "+ integer);
        });

        multicast.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    @Test
    void sink_unicast() {
        Sinks.Many<Integer> unicast = Sinks.many().unicast().onBackpressureBuffer();

        unicast.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        unicast.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        Flux<Integer> integerFlux = unicast.asFlux();
        integerFlux.subscribe(integer -> {
            System.out.println("Subscriber 1: "+ integer);
        });

        unicast.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);
    }
}
