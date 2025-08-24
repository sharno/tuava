#!/bin/bash

# Build the project first
./gradlew build

# Run the counter app directly with java
java -cp "app/build/classes/java/main:examples/build/classes/java/main" org.tuava.examples.TodoApp
