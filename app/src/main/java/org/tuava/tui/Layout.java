package org.tuava.tui;

import java.util.List;
import java.util.regex.Pattern;

public class Layout {
    private static final Pattern ANSI_PATTERN = Pattern.compile("\\u001B\\[[0-9;]*m");

    private static int visibleLength(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return ANSI_PATTERN.matcher(text).replaceAll("").length();
    }

    private static String spaces(int count) {
        return count <= 0 ? "" : " ".repeat(count);
    }

    public static String center(String text, int width) {
        int vis = visibleLength(text);
        if (vis >= width) {
            return text;
        }
        int left = (width - vis) / 2;
        int right = width - vis - left;
        return spaces(left) + text + spaces(right);
    }

    public static String padRight(String text, int width) {
        int vis = visibleLength(text);
        if (vis >= width) {
            return text;
        }
        return text + spaces(width - vis);
    }

    public static String padLeft(String text, int width) {
        int vis = visibleLength(text);
        if (vis >= width) {
            return text;
        }
        return spaces(width - vis) + text;
    }

    public static String box(String content, int width, int height) {
        StringBuilder sb = new StringBuilder();

        // Top border
        sb.append("┌").append("─".repeat(width - 2)).append("┐\n");

        // Content lines
        String[] lines = content.split("\n");
        for (int i = 0; i < height - 2; i++) {
            sb.append("│");
            if (i < lines.length) {
                String line = lines[i];
                if (visibleLength(line) > width - 2) {
                    // naive truncate by code units; acceptable for ASCII demos
                    line = line.substring(0, width - 2);
                }
                sb.append(padRight(line, width - 2));
            } else {
                sb.append(" ".repeat(width - 2));
            }
            sb.append("│\n");
        }

        // Bottom border
        sb.append("└").append("─".repeat(width - 2)).append("┘");

        return sb.toString();
    }

    public static String verticalJoin(List<String> components) {
        return String.join("\n", components);
    }

    public static String horizontalJoin(List<String> components) {
        if (components.isEmpty()) {
            return "";
        }

        // Split each component into lines
        List<String[]> componentLines = components.stream()
                .map(c -> c.split("\n"))
                .toList();

        // Find max height
        int maxHeight = componentLines.stream()
                .mapToInt(lines -> lines.length)
                .max()
                .orElse(0);

        StringBuilder result = new StringBuilder();

        for (int row = 0; row < maxHeight; row++) {
            for (int comp = 0; comp < componentLines.size(); comp++) {
                String[] lines = componentLines.get(comp);
                if (row < lines.length) {
                    result.append(lines[row]);
                } else {
                    // Pad with spaces to match width of first line
                    if (lines.length > 0) {
                        int w = visibleLength(lines[0]);
                        result.append(" ".repeat(w));
                    }
                }
            }
            if (row < maxHeight - 1) {
                result.append("\n");
            }
        }

        return result.toString();
    }
}
