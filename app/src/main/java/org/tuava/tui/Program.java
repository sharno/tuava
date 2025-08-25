package org.tuava.tui;

import java.io.IOException;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ConcurrentHashMap;

public class Program<Msg> {
    private final Terminal terminal;
    private final ExecutorService executor;
    private final ScheduledExecutorService scheduler;
    private volatile boolean running = false;
    private final Thread shutdownHook;
    private volatile boolean cleanedUp = false;
    private final BlockingQueue<Msg> messageQueue = new LinkedBlockingQueue<>();
    private final java.util.function.Function<Event, Optional<Msg>> eventToMessage;
    private final java.util.function.Function<Model<Msg>, List<Stream<Msg>>> streamsForModel;
    private final Map<String, AutoCloseable> activeStreams = new ConcurrentHashMap<>();

    public Program(java.util.function.Function<Event, Optional<Msg>> eventToMessage,
            java.util.function.Function<Model<Msg>, List<Stream<Msg>>> streamsForModel) {
        this.terminal = new Terminal();
        this.executor = Executors.newCachedThreadPool();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.eventToMessage = eventToMessage;
        this.streamsForModel = streamsForModel;
        this.shutdownHook = new Thread(() -> {
            try {
                cleanup();
            } catch (IOException ignored) {
            }
        });
        Runtime.getRuntime().addShutdownHook(this.shutdownHook);
    }

    public void run(Model<Msg> initialModel) throws IOException {
        terminal.enterRawMode();
        terminal.enterAlternateScreen();
        terminal.clear();
        terminal.hideCursor();

        running = true;
        Model<Msg> currentModel = initialModel;

        // Initial render
        render(currentModel);

        // Process initial effect
        processEffect(initialModel.init());
        // Start initial streams
        diffStreams(null, currentModel);

        try {
            while (running) {
                // Non-blocking: first drain any queued messages
                Msg queued = messageQueue.poll();
                if (queued != null) {
                    Update<Msg> upd = currentModel.update(queued);
                    if (upd.model() != currentModel) {
                        Model<Msg> previous = currentModel;
                        currentModel = upd.model();
                        render(currentModel);
                        diffStreams(previous, currentModel);
                    }
                    processEffect(upd.effect());
                    continue;
                }

                // Otherwise, read terminal event (blocking)
                Event event = terminal.readEvent();
                if (event == null) {
                    break;
                }

                // Map to message if possible
                Optional<Msg> maybeMsg = eventToMessage.apply(event);
                if (maybeMsg.isPresent()) {
                    Msg msg = maybeMsg.get();
                    Update<Msg> upd = currentModel.update(msg);
                    if (upd.model() != currentModel) {
                        Model<Msg> previous = currentModel;
                        currentModel = upd.model();
                        render(currentModel);
                        diffStreams(previous, currentModel);
                    }
                    processEffect(upd.effect());
                }
            }
        } finally {
            cleanup();
            try {
                Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
            } catch (IllegalStateException ignored) {
                // VM is already shutting down
            }
        }
    }

    private void render(Model<Msg> model) {
        terminal.clear();
        terminal.moveCursor(1, 1);
        String view = model.view().replace("\n", "\r\n");
        terminal.print(view);
    }

    private void processEffect(Effect<Msg> effect) {
        switch (effect) {
            case Effect.None<Msg> none -> {
            }
            case Effect.Pure<Msg> pure -> messageQueue.offer(pure.message());
            case Effect.Once<?, ?> o -> {
                @SuppressWarnings("unchecked")
                Effect.Once<Object, Msg> once = (Effect.Once<Object, Msg>) o;
                executor.submit(() -> {
                    Object r = once.supplier().get();
                    Msg m = once.mapper().apply(r);
                    if (m != null)
                        messageQueue.offer(m);
                });
            }
            case Effect.FromFuture<Msg> fut -> fut.future().thenAccept(messageQueue::offer);
            case Effect.Batch<Msg> batch -> {
                for (Effect<Msg> e : batch.effects()) {
                    processEffect(e);
                }
            }
            case Effect.Quit<Msg> q -> quit();
            default -> {
            }
        }
    }

    private void diffStreams(Model<Msg> previous, Model<Msg> current) {
        List<Stream<Msg>> desired = streamsForModel.apply(current);
        HashSet<String> desiredKeys = new HashSet<>();
        for (Stream<Msg> s : desired) {
            String key = s.key();
            desiredKeys.add(key);
            if (!activeStreams.containsKey(key)) {
                try {
                    AutoCloseable handle = s.start(messageQueue::offer, scheduler);
                    activeStreams.put(key, handle);
                } catch (Exception ignored) {
                }
            }
        }
        for (var entry : activeStreams.entrySet()) {
            if (!desiredKeys.contains(entry.getKey())) {
                try {
                    entry.getValue().close();
                } catch (Exception ignored) {
                }
                activeStreams.remove(entry.getKey());
            }
        }
    }

    public void quit() {
        running = false;
    }

    private void cleanup() throws IOException {
        if (cleanedUp) {
            return;
        }
        cleanedUp = true;
        try {
            terminal.showCursor();
            System.out.print(Terminal.ANSI.RESET);
            System.out.println();
            System.out.flush();
        } finally {
            terminal.exitAlternateScreen();
            terminal.exitRawMode();
            executor.shutdown();
            if (scheduler != null) {
                scheduler.shutdownNow();
            }
            for (var entry : activeStreams.entrySet()) {
                try {
                    entry.getValue().close();
                } catch (Exception ignored) {
                }
            }
            activeStreams.clear();
        }
    }
}
