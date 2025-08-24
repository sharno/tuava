package org.tuava.tui;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Program {
    private final Terminal terminal;
    private final ExecutorService executor;
    private volatile boolean running = false;
    private final Thread shutdownHook;
    private volatile boolean cleanedUp = false;

    public Program() {
        this.terminal = new Terminal();
        this.executor = Executors.newCachedThreadPool();
        this.shutdownHook = new Thread(() -> {
            try {
                cleanup();
            } catch (IOException ignored) {
            }
        });
        Runtime.getRuntime().addShutdownHook(this.shutdownHook);
    }

    public void run(Model initialModel) throws IOException {
        terminal.enterRawMode();
        terminal.enterAlternateScreen();
        terminal.clear();
        terminal.hideCursor();

        running = true;
        Model currentModel = initialModel;

        // Initial render
        render(currentModel);

        // Process initial command
        processCommand(initialModel.init());

        try {
            while (running) {
                Event event = terminal.readEvent();
                if (event == null) {
                    break;
                }

                // Handle quit events
                if (event instanceof Event.KeyEvent keyEvent) {
                    if (keyEvent.key() == Event.Key.CTRL_C ||
                            (keyEvent.key() == Event.Key.CHAR && "q".equals(keyEvent.sequence()))) {
                        break;
                    }
                }

                // Update model
                Model newModel = currentModel.update(event);
                if (newModel != currentModel) {
                    currentModel = newModel;
                    render(currentModel);
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

    private void render(Model model) {
        terminal.clear();
        terminal.moveCursor(1, 1);
        // Ensure CRLF so newline returns to column 1 even in raw mode
        String view = model.view().replace("\n", "\r\n");
        terminal.print(view);
    }

    private void processCommand(Command command) {
        switch (command) {
            case Command.None none -> {
                // Do nothing
            }
            case Command.Batch batch -> {
                for (Command cmd : batch.commands()) {
                    processCommand(cmd);
                }
            }
            case Command.Async<?> async -> {
                async.future().thenAcceptAsync(result -> {
                    @SuppressWarnings("unchecked")
                    var mapper = (java.util.function.Function<Object, Event>) async.mapper();
                    Event event = mapper.apply(result);
                    // In a real implementation, we'd need to send this event back to the main loop
                    // For now, this is a simplified version
                }, executor);
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
            // Ensure attributes reset and move to a new line for a clean shell state
            System.out.print(Terminal.ANSI.RESET);
            System.out.println();
            System.out.flush();
        } finally {
            terminal.exitAlternateScreen();
            terminal.exitRawMode();
            executor.shutdown();
        }
    }
}
