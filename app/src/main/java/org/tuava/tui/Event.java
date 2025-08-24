package org.tuava.tui;

public sealed interface Event permits Event.KeyEvent, Event.MouseEvent, Event.ResizeEvent, Event.TickEvent {

    record KeyEvent(Key key, String sequence) implements Event {
    }

    record MouseEvent(int x, int y, MouseButton button, MouseAction action) implements Event {
    }

    record ResizeEvent(int width, int height) implements Event {
    }

    record TickEvent(long timestamp) implements Event {
    }

    enum Key {
        ENTER, ESCAPE, BACKSPACE, DELETE, TAB, SPACE,
        ARROW_UP, ARROW_DOWN, ARROW_LEFT, ARROW_RIGHT,
        HOME, END, PAGE_UP, PAGE_DOWN,
        F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12,
        CHAR, CTRL_C, CTRL_D, CTRL_Z, UNKNOWN
    }

    enum MouseButton {
        LEFT, RIGHT, MIDDLE, NONE
    }

    enum MouseAction {
        PRESS, RELEASE, MOVE, DRAG, SCROLL_UP, SCROLL_DOWN
    }
}
