# Tuava TUI Examples

This directory contains example applications demonstrating the Tuava TUI library features.

## Examples

### Counter App
A simple counter application that demonstrates:
- Basic event handling with pattern matching
- Keyboard input processing
- Styled text rendering
- Layout utilities

**Run with:**
```bash
# Option 1: Using Gradle (may have input issues in some terminals)
./gradlew examples:runCounter

# Option 2: Run directly with Java (recommended for full functionality)
./gradlew build
java -cp "app/build/classes/java/main:examples/build/classes/java/main" org.tuava.examples.CounterApp
```

**Controls:**
- `+` / `-`: Increment/decrement counter
- `↑` / `↓`: Arrow keys also increment/decrement
- `Enter`: Reset counter to 0
- `q`: Quit

### Todo App
A more complex todo list application that demonstrates:
- Complex state management with records
- Multiple application modes
- List navigation
- Text input handling
- Dynamic UI updates

**Run with:**
```bash
# Option 1: Using Gradle (may have input issues in some terminals)
./gradlew examples:runTodo

# Option 2: Run directly with Java (recommended for full functionality)
./gradlew build
java -cp "app/build/classes/java/main:examples/build/classes/java/main" org.tuava.examples.TodoApp
```

**Controls:**
- `j` / `k` or `↑` / `↓`: Navigate todos
- `Space` / `Enter`: Toggle todo completion
- `a`: Add new todo
- `d`: Delete selected todo
- `q`: Quit

## Java 21 Features Demonstrated

### Records
All models use records for immutable state:
```java
public record CounterModel(int count, String status) implements Model { ... }
```

### Sealed Interfaces
Events are defined as sealed interfaces for exhaustive pattern matching:
```java
public sealed interface Event permits Event.KeyEvent, Event.MouseEvent, ... { ... }
```

### Switch Expressions
Pattern matching with switch expressions:
```java
return switch (event) {
    case Event.KeyEvent keyEvent -> switch (keyEvent.key()) {
        case CHAR -> switch (keyEvent.sequence()) {
            case "+" -> new CounterModel(count + 1, "Incremented!");
            case "-" -> new CounterModel(count - 1, "Decremented!");
            default -> this;
        };
        default -> this;
    };
    default -> this;
};
```

### Pattern Matching
Destructuring in switch expressions:
```java
case Event.ResizeEvent resizeEvent -> 
    new CounterModel(count, "Resized: " + resizeEvent.width() + "x" + resizeEvent.height());
```

## Architecture

The Tuava TUI library follows the Elm Architecture pattern:
1. **Model**: Immutable state represented as records
2. **Update**: Pure functions that take events and return new models
3. **View**: Functions that render models to strings
4. **Commands**: Side effects (not fully implemented in this simple version)

This architecture combined with Java 21's pattern matching provides:
- Exhaustive checking at compile time
- Immutable state management
- Clear separation of concerns
- Type-safe event handling
