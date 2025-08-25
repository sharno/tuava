package org.tuava.tui;

public interface Model<Msg> {
    Update<Msg> update(Msg message);

    String view();

    default Effect<Msg> init() {
        return Effect.none();
    }
}
