package org.tuava.examples;

import org.tuava.tui.*;

public class CounterApp {

    public record CounterModel(int count, String status) implements Model {

        public static CounterModel initial() {
            return new CounterModel(0, "Press +/- to change counter, q to quit");
        }

        @Override
        public Model update(Event event) {
            return switch (event) {
                case Event.KeyEvent keyEvent -> switch (keyEvent.key()) {
                    case CHAR -> switch (keyEvent.sequence()) {
                        case "+" -> new CounterModel(count + 1, "Incremented!");
                        case "-" -> new CounterModel(count - 1, "Decremented!");
                        case "q" -> this;
                        default -> new CounterModel(count, "Unknown key: " + keyEvent.sequence());
                    };
                    case ARROW_UP -> new CounterModel(count + 1, "Up arrow pressed");
                    case ARROW_DOWN -> new CounterModel(count - 1, "Down arrow pressed");
                    case ENTER -> new CounterModel(0, "Counter reset!");
                    default -> new CounterModel(count, "Key pressed: " + keyEvent.key());
                };
                case Event.ResizeEvent resizeEvent ->
                    new CounterModel(count, "Terminal resized: " + resizeEvent.width() + "x" + resizeEvent.height());
                case Event.MouseEvent mouseEvent ->
                    new CounterModel(count,
                            "Mouse: " + mouseEvent.action() + " at " + mouseEvent.x() + "," + mouseEvent.y());
                case Event.TickEvent tickEvent ->
                    new CounterModel(count, "Tick: " + tickEvent.timestamp());
            };
        }

        @Override
        public String view() {
            Style titleStyle = Style.of().foreground(Style.Color.CYAN).withBold();
            Style counterStyle = Style.of().foreground(Style.Color.GREEN).withBold();
            Style statusStyle = Style.of().foreground(Style.Color.YELLOW);

            String title = titleStyle.render("┌─ Tuava Counter App ─┐");
            String counterDisplay = counterStyle.render("Count: " + count);
            String statusDisplay = statusStyle.render(status);
            String instructions = "Controls: +/- to change, ↑↓ arrows, Enter to reset, q to quit";

            return Layout.verticalJoin(java.util.List.of(
                    "",
                    Layout.center(title, 50),
                    "",
                    Layout.center(counterDisplay, 50),
                    "",
                    Layout.center(statusDisplay, 50),
                    "",
                    Layout.center(instructions, 50),
                    ""));
        }
    }

    public static void main(String[] args) {
        try {
            Program program = new Program();
            program.run(CounterModel.initial());
        } catch (Exception e) {
            System.err.println("Error running counter app: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
