package org.tuava.tui;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public sealed interface Command permits Command.None, Command.Batch, Command.Async {

    record None() implements Command {
    }

    record Batch(Command... commands) implements Command {
    }

    record Async<T>(CompletableFuture<T> future, java.util.function.Function<T, Event> mapper) implements Command {
    }

    static Command none() {
        return new None();
    }

    static Command batch(Command... commands) {
        return new Batch(commands);
    }

    static <T> Command async(Supplier<T> supplier, java.util.function.Function<T, Event> mapper) {
        return new Async<>(CompletableFuture.supplyAsync(supplier), mapper);
    }
}
