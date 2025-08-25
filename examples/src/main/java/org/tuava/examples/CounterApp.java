package org.tuava.examples;

import org.tuava.tui.*;

public class CounterApp {

    public record CounterModel(int count, String status) implements Model<Event> {

        public static CounterModel initial() {
            return new CounterModel(0, "Press +/- to change counter, q to quit");
        }

        @Override
        public Update<Event> update(Event event) {
            return switch (event) {
                case Event.KeyEvent keyEvent -> switch (keyEvent.key()) {
                    case CHAR -> switch (keyEvent.sequence()) {
                        case "+" -> Update.of(new CounterModel(count + 1, "Incremented!"));
                        case "-" -> Update.of(new CounterModel(count - 1, "Decremented!"));
                        case "q" -> Update.of(this, Effect.quit());
                        default ->
                            Update.of(new CounterModel(count, "Unknown key: " + keyEvent.sequence()));
                    };
                    case ARROW_UP -> Update.of(new CounterModel(count + 1, "Up arrow pressed"));
                    case ARROW_DOWN -> Update.of(new CounterModel(count - 1, "Down arrow pressed"));
                    case ENTER -> Update.of(new CounterModel(0, "Counter reset!"));
                    default -> Update.of(new CounterModel(count, "Key pressed: " + keyEvent.key()));
                };
                case Event.ResizeEvent resizeEvent -> Update.of(
                        new CounterModel(count,
                                "Terminal resized: " + resizeEvent.width() + "x" + resizeEvent.height()));
                case Event.MouseEvent mouseEvent -> Update.of(new CounterModel(count,
                        "Mouse: " + mouseEvent.action() + " at " + mouseEvent.x() + "," + mouseEvent.y()));
                case Event.TickEvent tickEvent -> Update.of(new CounterModel(count, "Tick: " + tickEvent.timestamp()));
            };
        }

        @Override
        public String view() {
            Element ui = Flex.column()
                    .align(Flex.Align.CENTER)
                    .justify(Flex.Justify.START)
                    .gap(1)
                    .width(50)
                    .children(java.util.List.of(
                            Text.bold().foreground(Style.Color.CYAN).build("┌─ Tuava Counter App ─┐"),
                            Text.bold().foreground(Style.Color.GREEN).build("Count: " + count),
                            Text.plain().foreground(Style.Color.YELLOW).build(status),
                            Text.plain().build("Controls: +/- to change, ↑↓ arrows, Enter to reset, q to quit")))
                    .build();

            return ui.render();
        }
    }

    public static void main(String[] args) {
        try {
            Program<Event> program = new Program<>(java.util.Optional::of, m -> java.util.List.of());
            program.run(CounterModel.initial());
        } catch (Exception e) {
            System.err.println("Error running counter app: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
