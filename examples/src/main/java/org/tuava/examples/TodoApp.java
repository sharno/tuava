package org.tuava.examples;

import org.tuava.tui.*;
import java.util.List;
import java.util.ArrayList;

public class TodoApp {

    public record TodoItem(String text, boolean completed) {
        public TodoItem toggle() {
            return new TodoItem(text, !completed);
        }
    }

    public enum AppMode {
        VIEWING, ADDING
    }

    public record TodoModel(
            List<TodoItem> todos,
            int selectedIndex,
            AppMode mode,
            String inputBuffer,
            String statusMessage) implements Model<Event> {

        public static TodoModel initial() {
            List<TodoItem> initialTodos = List.of(
                    new TodoItem("Learn Java 21 features", false),
                    new TodoItem("Build TUI library", true),
                    new TodoItem("Create example apps", false));
            return new TodoModel(initialTodos, 0, AppMode.VIEWING, "", "Welcome to Tuava Todo!");
        }

        public Builder toBuilder() {
            return new Builder(this);
        }

        public static final class Builder {
            private List<TodoItem> todos;
            private int selectedIndex;
            private AppMode mode;
            private String inputBuffer;
            private String statusMessage;

            public Builder(TodoModel m) {
                this.todos = m.todos;
                this.selectedIndex = m.selectedIndex;
                this.mode = m.mode;
                this.inputBuffer = m.inputBuffer;
                this.statusMessage = m.statusMessage;
            }

            public Builder todos(List<TodoItem> v) {
                this.todos = v;
                return this;
            }

            public Builder selectedIndex(int v) {
                this.selectedIndex = v;
                return this;
            }

            public Builder mode(AppMode v) {
                this.mode = v;
                return this;
            }

            public Builder inputBuffer(String v) {
                this.inputBuffer = v;
                return this;
            }

            public Builder statusMessage(String v) {
                this.statusMessage = v;
                return this;
            }

            public TodoModel build() {
                return new TodoModel(todos, selectedIndex, mode, inputBuffer, statusMessage);
            }
        }

        @Override
        public Update<Event> update(Event event) {
            return switch (event) {
                case Event.KeyEvent keyEvent -> handleKeyEvent(keyEvent);
                default -> Update.of(this.toBuilder()
                        .statusMessage("Event: " + event.getClass().getSimpleName())
                        .build());
            };
        }

        private Update<Event> handleKeyEvent(Event.KeyEvent keyEvent) {
            return switch (mode) {
                case VIEWING -> handleViewingMode(keyEvent);
                case ADDING -> handleAddingMode(keyEvent);
            };
        }

        private Update<Event> handleViewingMode(Event.KeyEvent keyEvent) {
            return switch (keyEvent.key()) {
                case CHAR -> switch (keyEvent.sequence()) {
                    case "q" -> Update.of(this, Effect.quit());
                    case "j" -> Update.of(moveDown());
                    case "k" -> Update.of(moveUp());
                    case " " -> Update.of(toggleSelected());
                    case "a" -> Update.of(this.toBuilder()
                            .mode(AppMode.ADDING)
                            .inputBuffer("")
                            .statusMessage("Enter new todo:")
                            .build());
                    case "d" -> Update.of(deleteSelected());
                    default -> Update.of(this.toBuilder()
                            .statusMessage("Unknown key: " + keyEvent.sequence())
                            .build());
                };
                case ARROW_DOWN -> Update.of(moveDown());
                case ARROW_UP -> Update.of(moveUp());
                case ENTER -> Update.of(toggleSelected());
                default -> Update.of(this.toBuilder()
                        .statusMessage("Key: " + keyEvent.key())
                        .build());
            };
        }

        private Update<Event> handleAddingMode(Event.KeyEvent keyEvent) {
            return switch (keyEvent.key()) {
                case CHAR -> Update.of(this.toBuilder()
                        .inputBuffer(inputBuffer + keyEvent.sequence())
                        .build());
                case BACKSPACE -> Update.of(this.toBuilder()
                        .inputBuffer(inputBuffer.isEmpty() ? "" : inputBuffer.substring(0, inputBuffer.length() - 1))
                        .build());
                case ENTER -> {
                    if (!inputBuffer.trim().isEmpty()) {
                        List<TodoItem> newTodos = new ArrayList<>(todos);
                        newTodos.add(new TodoItem(inputBuffer.trim(), false));
                        yield Update.of(this.toBuilder()
                                .todos(newTodos)
                                .mode(AppMode.VIEWING)
                                .inputBuffer("")
                                .statusMessage("Added: " + inputBuffer.trim())
                                .build());
                    } else {
                        yield Update.of(this.toBuilder()
                                .mode(AppMode.VIEWING)
                                .inputBuffer("")
                                .statusMessage("Cancelled adding todo")
                                .build());
                    }
                }
                case ESCAPE -> Update.of(this.toBuilder()
                        .mode(AppMode.VIEWING)
                        .inputBuffer("")
                        .statusMessage("Cancelled adding todo")
                        .build());
                default -> Update.of(this);
            };
        }

        private TodoModel moveDown() {
            int newIndex = selectedIndex < todos.size() - 1 ? selectedIndex + 1 : selectedIndex;
            return this.toBuilder()
                    .selectedIndex(newIndex)
                    .statusMessage(newIndex != selectedIndex ? "Moved down" : "At bottom")
                    .build();
        }

        private TodoModel moveUp() {
            int newIndex = selectedIndex > 0 ? selectedIndex - 1 : selectedIndex;
            return this.toBuilder()
                    .selectedIndex(newIndex)
                    .statusMessage(newIndex != selectedIndex ? "Moved up" : "At top")
                    .build();
        }

        private TodoModel toggleSelected() {
            if (todos.isEmpty()) {
                return this.toBuilder().statusMessage("No todos to toggle").build();
            }

            List<TodoItem> newTodos = new ArrayList<>(todos);
            TodoItem current = newTodos.get(selectedIndex);
            newTodos.set(selectedIndex, current.toggle());

            return this.toBuilder()
                    .todos(newTodos)
                    .statusMessage(current.completed ? "Marked as incomplete" : "Marked as complete")
                    .build();
        }

        private TodoModel deleteSelected() {
            if (todos.isEmpty()) {
                return this.toBuilder().statusMessage("No todos to delete").build();
            }

            List<TodoItem> newTodos = new ArrayList<>(todos);
            TodoItem deleted = newTodos.remove(selectedIndex);
            int newSelectedIndex = selectedIndex >= newTodos.size() ? Math.max(0, newTodos.size() - 1) : selectedIndex;

            return this.toBuilder()
                    .todos(newTodos)
                    .selectedIndex(newSelectedIndex)
                    .statusMessage("Deleted: " + deleted.text())
                    .build();
        }

        @Override
        public String view() {
            java.util.List<Element> listItems = new java.util.ArrayList<>();

            if (todos.isEmpty()) {
                listItems.add(Text.plain().build("No todos yet! Press 'a' to add one."));
            } else {
                for (int i = 0; i < todos.size(); i++) {
                    TodoItem todo = todos.get(i);
                    String checkbox = todo.completed() ? "[✓]" : "[ ]";
                    String text = checkbox + " " + todo.text();

                    String line = text;
                    if (i == selectedIndex && mode == AppMode.VIEWING) {
                        line = Style.of().background(Style.Color.BLUE).foreground(Style.Color.WHITE)
                                .render(" " + line + " ");
                    } else {
                        line = (todo.completed() ? Style.of().foreground(Style.Color.GREEN)
                                : Style.of().foreground(Style.Color.WHITE)).render(line);
                    }

                    listItems.add(Text.plain().build(line));
                }
            }

            Element ui = Flex.column()
                    .align(Flex.Align.CENTER)
                    .justify(Flex.Justify.START)
                    .gap(1)
                    .width(60)
                    .children(java.util.List.of(
                            Text.bold().foreground(Style.Color.MAGENTA).build("┌─ Tuava Todo App ─┐"),
                            Flex.column().gap(0).align(Flex.Align.START).children(listItems).build(),
                            ifAdding(),
                            Text.plain().foreground(Style.Color.YELLOW).build(statusMessage),
                            Text.plain().build(switch (mode) {
                                case VIEWING ->
                                    "j/k or ↑↓: navigate | Space/Enter: toggle | a: add | d: delete | q: quit";
                                case ADDING -> "Type todo text | Enter: save | Escape: cancel";
                            })))
                    .build();

            return ui.render();
        }

        private Element ifAdding() {
            if (mode == AppMode.ADDING) {
                String inputLine = "New todo: " + inputBuffer + "█";
                return Text.plain().foreground(Style.Color.CYAN).build(inputLine);
            }
            return Text.plain().build("");
        }
    }

    public static void main(String[] args) {
        try {
            Program<Event> program = new Program<>(java.util.Optional::of, m -> java.util.List.of());
            program.run(TodoModel.initial());
        } catch (Exception e) {
            System.err.println("Error running todo app: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
