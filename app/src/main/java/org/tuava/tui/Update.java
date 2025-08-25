package org.tuava.tui;

public record Update<Msg>(Model<Msg> model, Effect<Msg> effect) {
    public Update(Model<Msg> model) {
        this(model, Effect.none());
    }

    public static <Msg> Update<Msg> of(Model<Msg> model, Effect<Msg> effect) {
        return new Update<>(model, effect);
    }

    public static <Msg> Update<Msg> of(Model<Msg> model) {
        return new Update<>(model, Effect.none());
    }
}
