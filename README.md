# Tuava TUI Library

A modern Terminal User Interface (TUI) library for Java 21, inspired by Bubble Tea. Tuava leverages Java 21's latest features including records, sealed interfaces, pattern matching, and switch expressions to provide a type-safe, functional approach to building terminal applications.

## Features

- **Modern Java 21**: Utilizes records, sealed interfaces, pattern matching, and switch expressions
- **Functional Architecture**: Based on the Elm Architecture pattern (Model-Update-View)
- **Type Safety**: Exhaustive pattern matching ensures compile-time safety
- **Immutable State**: All models are immutable records
- **Rich Styling**: ANSI color and styling support
- **Event System**: Comprehensive keyboard and mouse event handling
- **Layout Utilities**: Box drawing, centering, padding, and layout helpers

## Architecture

Tuava follows the Elm Architecture pattern:

1. **Model**: Immutable state represented as records
2. **Update**: Pure functions that take events and return new models  
3. **View**: Functions that render models to strings
4. **Commands**: Side effects (async operations, etc.)

```java
public record MyModel(int count) implements Model {
    @Override
    public Model update(Event event) {
        return switch (event) {
            case Event.KeyEvent keyEvent -> switch (keyEvent.key()) {
                case CHAR -> switch (keyEvent.sequence()) {
                    case "+" -> new MyModel(count + 1);
                    case "-" -> new MyModel(count - 1);
                    default -> this;
                };
                default -> this;
            };
            default -> this;
        };
    }
    
    @Override
    public String view() {
        return "Count: " + count;
    }
}
```

## Quick Start

### 1. Add Tuava to your project

```kotlin
// build.gradle.kts
dependencies {
    implementation(project(":app")) // or published artifact when available
}
```

### 2. Create a Model

```java
public record CounterModel(int count, String message) implements Model {
    public static CounterModel initial() {
        return new CounterModel(0, "Press +/- to change counter");
    }
    
    @Override
    public Model update(Event event) {
        return switch (event) {
            case Event.KeyEvent keyEvent -> switch (keyEvent.key()) {
                case CHAR -> switch (keyEvent.sequence()) {
                    case "+" -> new CounterModel(count + 1, "Incremented!");
                    case "-" -> new CounterModel(count - 1, "Decremented!");
                    case "q" -> { System.exit(0); yield this; }
                    default -> this;
                };
                default -> this;
            };
            default -> this;
        };
    }
    
    @Override
    public String view() {
        Style titleStyle = Style.of().foreground(Style.Color.CYAN).withBold();
        return titleStyle.render("Count: " + count) + "\n" + message;
    }
}
```

### 3. Run your application

```java
public static void main(String[] args) {
    try {
        Program program = new Program();
        program.run(CounterModel.initial());
    } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
    }
}
```

## Examples

The `examples/` directory contains several example applications:

### Counter App
```bash
# Using Gradle
./gradlew examples:runCounter

# Or run directly (recommended for full terminal functionality)
./gradlew build
java -cp "app/build/classes/java/main:examples/build/classes/java/main" org.tuava.examples.CounterApp
```
A simple counter demonstrating basic event handling and styling.

### Todo App  
```bash
# Using Gradle
./gradlew examples:runTodo

# Or run directly (recommended for full terminal functionality)
./gradlew build
java -cp "app/build/classes/java/main:examples/build/classes/java/main" org.tuava.examples.TodoApp
```
A more complex todo list application showing:
- Multiple application modes
- List navigation
- Text input handling
- Dynamic UI updates

## Java 21 Features Showcased

### Sealed Interfaces
Events are defined as sealed interfaces for exhaustive pattern matching:

```java
public sealed interface Event permits Event.KeyEvent, Event.MouseEvent, Event.ResizeEvent, Event.TickEvent {
    record KeyEvent(Key key, String sequence) implements Event {}
    record MouseEvent(int x, int y, MouseButton button, MouseAction action) implements Event {}
    // ...
}
```

### Pattern Matching with Switch Expressions
Handle events with exhaustive pattern matching:

```java
return switch (event) {
    case Event.KeyEvent keyEvent -> switch (keyEvent.key()) {
        case CHAR -> handleChar(keyEvent.sequence());
        case ARROW_UP -> moveUp();
        case ARROW_DOWN -> moveDown();
        default -> this;
    };
    case Event.ResizeEvent resizeEvent -> handleResize(resizeEvent.width(), resizeEvent.height());
    case Event.MouseEvent mouseEvent -> handleMouse(mouseEvent);
    case Event.TickEvent tickEvent -> handleTick(tickEvent.timestamp());
};
```

### Records for Immutable State
All models use records for immutable, thread-safe state:

```java
public record TodoModel(
    List<TodoItem> todos,
    int selectedIndex,
    AppMode mode,
    String inputBuffer,
    String statusMessage
) implements Model { ... }
```

## API Reference

### Core Classes

- **`Model`**: Interface for application state and logic
- **`Event`**: Sealed interface hierarchy for all events
- **`Program`**: Main application runner
- **`Terminal`**: Low-level terminal control
- **`Style`**: Text styling and colors
- **`Layout`**: Layout and formatting utilities

### Event Types

- **`KeyEvent`**: Keyboard input (characters, special keys)
- **`MouseEvent`**: Mouse input (clicks, movement, scrolling)
- **`ResizeEvent`**: Terminal resize events
- **`TickEvent`**: Timer/animation events

### Styling

```java
Style style = Style.of()
    .foreground(Style.Color.GREEN)
    .background(Style.Color.BLACK)
    .withBold()
    .withUnderline();

String styledText = style.render("Hello, World!");
```

### Layout Utilities

```java
// Center text
String centered = Layout.center("Hello", 20);

// Create a box
String boxed = Layout.box("Content", 30, 10);

// Join components
String joined = Layout.verticalJoin(List.of("Line 1", "Line 2", "Line 3"));
```

## Building

```bash
# Build the library
./gradlew build

# Run examples (using Gradle)
./gradlew examples:runCounter
./gradlew examples:runTodo

# Run examples directly (recommended for full functionality)
java -cp "app/build/classes/java/main:examples/build/classes/java/main" org.tuava.examples.CounterApp
java -cp "app/build/classes/java/main:examples/build/classes/java/main" org.tuava.examples.TodoApp
```

## Important Notes

- **Terminal Requirements**: The TUI applications require a proper terminal environment with TTY support
- **Input Handling**: For best results, run applications directly with `java` rather than through Gradle, especially in automated environments
- **Raw Mode**: The library attempts to set the terminal to raw mode using `stty` commands. This requires a Unix-like environment (Linux, macOS, WSL)
- **Graceful Fallback**: If raw mode cannot be enabled, the library will fall back to line-buffered input mode with reduced functionality

## Requirements

- Java 21 or later
- Unix-like terminal (Linux, macOS, WSL)
- Terminal with ANSI escape sequence support

## License

This project is open source. See LICENSE file for details.

## Contributing

Contributions are welcome! Please see CONTRIBUTING.md for guidelines.

---

Built with ❤️ and Java 21's modern features.
