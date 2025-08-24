package org.tuava.tui;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Terminal {
    private final PrintWriter out;
    private final InputStream in;
    private boolean rawMode = false;

    public Terminal() {
        this.out = new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8), true);
        this.in = System.in;
    }

    public void enterRawMode() throws IOException {
        if (!rawMode) {
            // Check if we're running in a proper terminal
            if (System.console() != null || isRunningInTerminal()) {
                try {
                    // Enable raw mode using stty - wait for completion
                    ProcessBuilder pb = new ProcessBuilder("stty", "raw", "-echo", "min", "1", "time", "0");
                    pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
                    pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                    pb.redirectError(ProcessBuilder.Redirect.INHERIT);

                    Process process = pb.start();
                    int exitCode = process.waitFor();
                    if (exitCode == 0) {
                        rawMode = true;
                        return;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted while setting raw mode", e);
                } catch (Exception e) {
                    // Fall through to non-raw mode
                }
            }

            // Fallback: simulate raw mode behavior without actual raw mode
            System.err.println("Warning: Running in non-terminal mode. Some features may not work correctly.");
            rawMode = true; // Set to true so readEvent works
        }
    }

    public void exitRawMode() throws IOException {
        if (rawMode) {
            try {
                // Only try to restore if we're in a real terminal
                if (System.console() != null || isRunningInTerminal()) {
                    ProcessBuilder pb = new ProcessBuilder("stty", "sane");
                    pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
                    pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                    pb.redirectError(ProcessBuilder.Redirect.INHERIT);

                    Process process = pb.start();
                    process.waitFor();
                }
                rawMode = false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while restoring terminal mode", e);
            } catch (Exception e) {
                // Ignore errors during cleanup
                rawMode = false;
            }
        }
    }

    private boolean isRunningInTerminal() {
        // Check if stdin is a TTY
        try {
            ProcessBuilder pb = new ProcessBuilder("test", "-t", "0");
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public void clear() {
        out.print(ANSI.CLEAR_SCREEN);
        out.print(ANSI.CURSOR_HOME);
        out.flush();
    }

    public void hideCursor() {
        out.print(ANSI.HIDE_CURSOR);
        out.flush();
    }

    public void showCursor() {
        out.print(ANSI.SHOW_CURSOR);
        out.print(ANSI.RESET);
        out.flush();
    }

    public void enterAlternateScreen() {
        out.print(ANSI.ENTER_ALTERNATE_SCREEN);
        out.flush();
    }

    public void exitAlternateScreen() {
        out.print(ANSI.EXIT_ALTERNATE_SCREEN);
        out.flush();
    }

    public void moveCursor(int row, int col) {
        out.printf(ANSI.CURSOR_POSITION, row, col);
        out.flush();
    }

    public void print(String text) {
        out.print(text);
        out.flush();
    }

    public void println(String text) {
        out.println(text);
        out.flush();
    }

    public TerminalSize getSize() {
        try {
            Process process = Runtime.getRuntime().exec(new String[] { "stty", "size" });
            process.waitFor();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length == 2) {
                        int rows = Integer.parseInt(parts[0]);
                        int cols = Integer.parseInt(parts[1]);
                        return new TerminalSize(cols, rows);
                    }
                }
            }
        } catch (Exception e) {
            // Fallback to default size
        }
        return new TerminalSize(80, 24);
    }

    public Event readEvent() throws IOException {
        // Ensure we're in raw mode before reading
        if (!rawMode) {
            throw new IOException("Terminal not in raw mode - call enterRawMode() first");
        }

        int ch = in.read();
        if (ch == -1) {
            return null;
        }

        return switch (ch) {
            case 3 -> new Event.KeyEvent(Event.Key.CTRL_C, "^C");
            case 4 -> new Event.KeyEvent(Event.Key.CTRL_D, "^D");
            case 9 -> new Event.KeyEvent(Event.Key.TAB, "\t");
            case 10, 13 -> new Event.KeyEvent(Event.Key.ENTER, "\n");
            case 27 -> handleEscapeSequence();
            case 127 -> new Event.KeyEvent(Event.Key.BACKSPACE, "\b");
            default -> new Event.KeyEvent(Event.Key.CHAR, String.valueOf((char) ch));
        };
    }

    private Event handleEscapeSequence() throws IOException {
        int next = in.read();
        if (next == -1) {
            return new Event.KeyEvent(Event.Key.ESCAPE, "\u001b");
        }

        if (next == '[') {
            int code = in.read();
            return switch (code) {
                case 'A' -> new Event.KeyEvent(Event.Key.ARROW_UP, "\u001b[A");
                case 'B' -> new Event.KeyEvent(Event.Key.ARROW_DOWN, "\u001b[B");
                case 'C' -> new Event.KeyEvent(Event.Key.ARROW_RIGHT, "\u001b[C");
                case 'D' -> new Event.KeyEvent(Event.Key.ARROW_LEFT, "\u001b[D");
                case 'H' -> new Event.KeyEvent(Event.Key.HOME, "\u001b[H");
                case 'F' -> new Event.KeyEvent(Event.Key.END, "\u001b[F");
                default -> new Event.KeyEvent(Event.Key.UNKNOWN, "\u001b[" + (char) code);
            };
        }

        return new Event.KeyEvent(Event.Key.UNKNOWN, "\u001b" + (char) next);
    }

    public record TerminalSize(int width, int height) {
    }

    public static class ANSI {
        public static final String RESET = "\u001b[0m";
        public static final String CLEAR_SCREEN = "\u001b[2J";
        public static final String CURSOR_HOME = "\u001b[H";
        public static final String HIDE_CURSOR = "\u001b[?25l";
        public static final String SHOW_CURSOR = "\u001b[?25h";
        public static final String CURSOR_POSITION = "\u001b[%d;%dH";

        // Colors
        public static final String BLACK = "\u001b[30m";
        public static final String RED = "\u001b[31m";
        public static final String GREEN = "\u001b[32m";
        public static final String YELLOW = "\u001b[33m";
        public static final String BLUE = "\u001b[34m";
        public static final String MAGENTA = "\u001b[35m";
        public static final String CYAN = "\u001b[36m";
        public static final String WHITE = "\u001b[37m";

        // Background colors
        public static final String BG_BLACK = "\u001b[40m";
        public static final String BG_RED = "\u001b[41m";
        public static final String BG_GREEN = "\u001b[42m";
        public static final String BG_YELLOW = "\u001b[43m";
        public static final String BG_BLUE = "\u001b[44m";
        public static final String BG_MAGENTA = "\u001b[45m";
        public static final String BG_CYAN = "\u001b[46m";
        public static final String BG_WHITE = "\u001b[47m";

        // Styles
        public static final String BOLD = "\u001b[1m";
        public static final String DIM = "\u001b[2m";
        public static final String ITALIC = "\u001b[3m";
        public static final String UNDERLINE = "\u001b[4m";
        public static final String BLINK = "\u001b[5m";
        public static final String REVERSE = "\u001b[7m";
        public static final String STRIKETHROUGH = "\u001b[9m";

        // Alternate screen buffer
        public static final String ENTER_ALTERNATE_SCREEN = "\u001b[?1049h";
        public static final String EXIT_ALTERNATE_SCREEN = "\u001b[?1049l";
    }
}
