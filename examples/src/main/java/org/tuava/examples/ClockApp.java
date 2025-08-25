package org.tuava.examples;

import org.tuava.tui.*;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ClockApp {

    public enum Msg {
        TICK, QUIT
    }

    public record ClockModel(String timeText, String hint) implements Model<Msg> {
        private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

        public static ClockModel initial() {
            return new ClockModel(LocalTime.now().format(FORMAT), "Press q to quit");
        }

        @Override
        public Update<Msg> update(Msg msg) {
            return switch (msg) {
                case TICK -> Update.of(new ClockModel(LocalTime.now().format(FORMAT), hint));
                case QUIT -> Update.of(this, Effect.quit());
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

    private static java.util.Optional<Msg> mapEventToMsg(Event e) {
        if (e instanceof Event.KeyEvent k && k.key() == Event.Key.CHAR && "q".equals(k.sequence())) {
            return java.util.Optional.of(Msg.QUIT);
        }
        return java.util.Optional.empty();
    }

    public static void main(String[] args) {
        try {
            Program<Msg> program = new Program<>(
                    ClockApp::mapEventToMsg,
                    m -> java.util.List.of(
                            Stream.interval("clock", Duration.ofSeconds(1), () -> Msg.TICK)));
            program.run(ClockModel.initial());
        } catch (Exception e) {
            System.err.println("Error running clock app: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
