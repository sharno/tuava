package org.tuava.examples;

import org.tuava.tui.*;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ClockApp {

    public record ClockModel(String timeText, String hint) implements Model<Event> {
        private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

        public static ClockModel initial() {
            return new ClockModel(LocalTime.now().format(FORMAT), "Press q to quit");
        }

        @Override
        public Update<Event> update(Event event) {
            return switch (event) {
                case Event.TickEvent tick -> Update.of(new ClockModel(LocalTime.now().format(FORMAT), hint));
                case Event.KeyEvent key -> switch (key.key()) {
                    case CHAR -> switch (key.sequence()) {
                        case "q" -> Update.of(this, Effect.quit());
                        default -> Update.of(this);
                    };
                    default -> Update.of(this);
                };
                default -> Update.of(this);
            };
        }

        @Override
        public String view() {
            String title = Style.of().foreground(Style.Color.CYAN).withBold().render("┌─ Tuava Clock ─┐");
            String time = Style.of().foreground(Style.Color.GREEN).withBold().render("Time: " + timeText);
            String help = Style.of().foreground(Style.Color.YELLOW).render(hint);
            return String.join("\n", title, time, help);
        }
    }

    public static void main(String[] args) {
        try {
            Program<Event> program = new Program<>(
                    java.util.Optional::of,
                    m -> java.util.List.of(
                            Stream.interval("clock", Duration.ofSeconds(1),
                                    () -> new Event.TickEvent(System.currentTimeMillis()))));
            program.run(ClockModel.initial());
        } catch (Exception e) {
            System.err.println("Error running clock app: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
