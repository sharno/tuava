package org.tuava.tui;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public sealed interface Effect<Msg>
        permits Effect.None, Effect.Pure, Effect.Once, Effect.Batch, Effect.Quit, Effect.FromFuture {

    record None<Msg>() implements Effect<Msg> {
    }

    record Pure<Msg>(Msg message) implements Effect<Msg> {
    }

    record Once<T, Msg>(Supplier<T> supplier, Function<T, Msg> mapper) implements Effect<Msg> {
    }

    record Batch<Msg>(Effect<Msg>... effects) implements Effect<Msg> {
    }

    record Quit<Msg>() implements Effect<Msg> {
    }

    record FromFuture<Msg>(CompletableFuture<Msg> future) implements Effect<Msg> {
    }

    static <Msg> Effect<Msg> none() {
        return new None<>();
    }

    static <Msg> Effect<Msg> pure(Msg message) {
        return new Pure<>(message);
    }

    static <T, Msg> Effect<Msg> once(Supplier<T> supplier, Function<T, Msg> mapper) {
        return new Once<>(supplier, mapper);
    }

    @SafeVarargs
    static <Msg> Effect<Msg> batch(Effect<Msg>... effects) {
        return new Batch<>(effects);
    }

    static <Msg> Effect<Msg> fromFuture(CompletableFuture<Msg> future) {
        return new FromFuture<>(future);
    }

    static <Msg> Effect<Msg> quit() {
        return new Quit<>();
    }
}
