package org.tuava.tui;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Stream<Msg> {
    String key();

    AutoCloseable start(Consumer<Msg> emit, java.util.concurrent.ScheduledExecutorService executor) throws Exception;

    static <Msg> Stream<Msg> interval(String key, Duration period, Supplier<Msg> supplier) {
        return new Stream<Msg>() {
            @Override
            public String key() {
                return key;
            }

            @Override
            public AutoCloseable start(Consumer<Msg> emit, java.util.concurrent.ScheduledExecutorService executor) {
                var future = executor.scheduleAtFixedRate(() -> emit.accept(supplier.get()),
                        period.toMillis(), period.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
                return () -> future.cancel(true);
            }
        };
    }

    static <Msg> Stream<Msg> resize(String key, Function<Terminal.TerminalSize, Msg> map,
            Supplier<Terminal.TerminalSize> getSize) {
        return new Stream<Msg>() {
            @Override
            public String key() {
                return key;
            }

            @Override
            public AutoCloseable start(Consumer<Msg> emit, java.util.concurrent.ScheduledExecutorService executor) {
                final var last = new java.util.concurrent.atomic.AtomicReference<>(getSize.get());
                var future = executor.scheduleAtFixedRate(() -> {
                    var now = getSize.get();
                    var prev = last.getAndSet(now);
                    if (prev == null || prev.width() != now.width() || prev.height() != now.height()) {
                        emit.accept(map.apply(now));
                    }
                }, 200, 200, java.util.concurrent.TimeUnit.MILLISECONDS);
                return () -> future.cancel(true);
            }
        };
    }
}
