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
            String statusMessage) implements Model {

        public static TodoModel initial() {
            List<TodoItem> initialTodos = List.of(
                    new TodoItem("Learn Java 21 features", false),
                    new TodoItem("Build TUI library", true),
                    new TodoItem("Create example apps", false));
            return new TodoModel(initialTodos, 0, AppMode.VIEWING, "", "Welcome to Tuava Todo!");
        }

        @Override
        public Model update(Event event) {
            return switch (event) {
                case Event.KeyEvent keyEvent -> handleKeyEvent(keyEvent);
                default -> new TodoModel(todos, selectedIndex, mode, inputBuffer,
                        "Event: " + event.getClass().getSimpleName());
            };
        }

        private TodoModel handleKeyEvent(Event.KeyEvent keyEvent) {
            return switch (mode) {
                case VIEWING -> handleViewingMode(keyEvent);
                case ADDING -> handleAddingMode(keyEvent);
            };
        }

        private TodoModel handleViewingMode(Event.KeyEvent keyEvent) {
            return switch (keyEvent.key()) {
                case CHAR -> switch (keyEvent.sequence()) {
                    case "q" -> this;
                    case "j" -> moveDown();
                    case "k" -> moveUp();
                    case " " -> toggleSelected();
                    case "a" -> new TodoModel(todos, selectedIndex, AppMode.ADDING, "", "Enter new todo:");
                    case "d" -> deleteSelected();
                    default -> new TodoModel(todos, selectedIndex, mode, inputBuffer,
                            "Unknown key: " + keyEvent.sequence());
                };
                case ARROW_DOWN -> moveDown();
                case ARROW_UP -> moveUp();
                case ENTER -> toggleSelected();
                default -> new TodoModel(todos, selectedIndex, mode, inputBuffer,
                        "Key: " + keyEvent.key());
            };
        }

        private TodoModel handleAddingMode(Event.KeyEvent keyEvent) {
            return switch (keyEvent.key()) {
                case CHAR -> new TodoModel(todos, selectedIndex, mode,
                        inputBuffer + keyEvent.sequence(), statusMessage);
                case BACKSPACE -> new TodoModel(todos, selectedIndex, mode,
                        inputBuffer.isEmpty() ? "" : inputBuffer.substring(0, inputBuffer.length() - 1),
                        statusMessage);
                case ENTER -> {
                    if (!inputBuffer.trim().isEmpty()) {
                        List<TodoItem> newTodos = new ArrayList<>(todos);
                        newTodos.add(new TodoItem(inputBuffer.trim(), false));
                        yield new TodoModel(newTodos, selectedIndex, AppMode.VIEWING, "",
                                "Added: " + inputBuffer.trim());
                    } else {
                        yield new TodoModel(todos, selectedIndex, AppMode.VIEWING, "",
                                "Cancelled adding todo");
                    }
                }
                case ESCAPE -> new TodoModel(todos, selectedIndex, AppMode.VIEWING, "",
                        "Cancelled adding todo");
                default -> this;
            };
        }

        private TodoModel moveDown() {
            int newIndex = selectedIndex < todos.size() - 1 ? selectedIndex + 1 : selectedIndex;
            return new TodoModel(todos, newIndex, mode, inputBuffer,
                    newIndex != selectedIndex ? "Moved down" : "At bottom");
        }

        private TodoModel moveUp() {
            int newIndex = selectedIndex > 0 ? selectedIndex - 1 : selectedIndex;
            return new TodoModel(todos, newIndex, mode, inputBuffer,
                    newIndex != selectedIndex ? "Moved up" : "At top");
        }

        private TodoModel toggleSelected() {
            if (todos.isEmpty()) {
                return new TodoModel(todos, selectedIndex, mode, inputBuffer, "No todos to toggle");
            }

            List<TodoItem> newTodos = new ArrayList<>(todos);
            TodoItem current = newTodos.get(selectedIndex);
            newTodos.set(selectedIndex, current.toggle());

            return new TodoModel(newTodos, selectedIndex, mode, inputBuffer,
                    current.completed ? "Marked as incomplete" : "Marked as complete");
        }

        private TodoModel deleteSelected() {
            if (todos.isEmpty()) {
                return new TodoModel(todos, selectedIndex, mode, inputBuffer, "No todos to delete");
            }

            List<TodoItem> newTodos = new ArrayList<>(todos);
            TodoItem deleted = newTodos.remove(selectedIndex);
            int newSelectedIndex = selectedIndex >= newTodos.size() ? Math.max(0, newTodos.size() - 1) : selectedIndex;

            return new TodoModel(newTodos, newSelectedIndex, mode, inputBuffer,
                    "Deleted: " + deleted.text());
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
                        line = Style.of().background(Style.Color.BLUE).foreground(Style.Color.WHITE).render(" " + line + " ");
                    } else {
                        line = (todo.completed() ? Style.of().foreground(Style.Color.GREEN) : Style.of().foreground(Style.Color.WHITE)).render(line);
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
                                case VIEWING -> "j/k or ↑↓: navigate | Space/Enter: toggle | a: add | d: delete | q: quit";
                                case ADDING -> "Type todo text | Enter: save | Escape: cancel";
                            })
                    ))
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
            Program program = new Program();
            program.run(TodoModel.initial());
        } catch (Exception e) {
            System.err.println("Error running todo app: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
