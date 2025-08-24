package org.tuava.tui;

public interface Model {
    Model update(Event event);

    String view();

    default Command init() {
        return Command.none();
    }
}
