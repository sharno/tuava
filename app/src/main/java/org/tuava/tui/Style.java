package org.tuava.tui;

public record Style(
        Color foreground,
        Color background,
        boolean bold,
        boolean italic,
        boolean underline,
        boolean reverse) {

    public static Style of() {
        return new Style(Color.DEFAULT, Color.DEFAULT, false, false, false, false);
    }

    public Style foreground(Color color) {
        return new Style(color, background, bold, italic, underline, reverse);
    }

    public Style background(Color color) {
        return new Style(foreground, color, bold, italic, underline, reverse);
    }

    public Style withBold() {
        return new Style(foreground, background, true, italic, underline, reverse);
    }

    public Style withItalic() {
        return new Style(foreground, background, bold, true, underline, reverse);
    }

    public Style withUnderline() {
        return new Style(foreground, background, bold, italic, true, reverse);
    }

    public Style withReverse() {
        return new Style(foreground, background, bold, italic, underline, true);
    }

    public String render(String text) {
        StringBuilder sb = new StringBuilder();

        if (foreground != Color.DEFAULT) {
            sb.append(foreground.ansiCode());
        }
        if (background != Color.DEFAULT) {
            sb.append(background.ansiBackgroundCode());
        }
        if (bold) {
            sb.append(Terminal.ANSI.BOLD);
        }
        if (italic) {
            sb.append(Terminal.ANSI.ITALIC);
        }
        if (underline) {
            sb.append(Terminal.ANSI.UNDERLINE);
        }
        if (reverse) {
            sb.append(Terminal.ANSI.REVERSE);
        }

        sb.append(text);
        sb.append(Terminal.ANSI.RESET);

        return sb.toString();
    }

    public enum Color {
        DEFAULT("", ""),
        BLACK(Terminal.ANSI.BLACK, Terminal.ANSI.BG_BLACK),
        RED(Terminal.ANSI.RED, Terminal.ANSI.BG_RED),
        GREEN(Terminal.ANSI.GREEN, Terminal.ANSI.BG_GREEN),
        YELLOW(Terminal.ANSI.YELLOW, Terminal.ANSI.BG_YELLOW),
        BLUE(Terminal.ANSI.BLUE, Terminal.ANSI.BG_BLUE),
        MAGENTA(Terminal.ANSI.MAGENTA, Terminal.ANSI.BG_MAGENTA),
        CYAN(Terminal.ANSI.CYAN, Terminal.ANSI.BG_CYAN),
        WHITE(Terminal.ANSI.WHITE, Terminal.ANSI.BG_WHITE);

        private final String ansiCode;
        private final String ansiBackgroundCode;

        Color(String ansiCode, String ansiBackgroundCode) {
            this.ansiCode = ansiCode;
            this.ansiBackgroundCode = ansiBackgroundCode;
        }

        public String ansiCode() {
            return ansiCode;
        }

        public String ansiBackgroundCode() {
            return ansiBackgroundCode;
        }
    }
}
