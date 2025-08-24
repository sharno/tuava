plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":app"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "org.tuava.examples.CounterApp"
}

// Task to run the counter example
tasks.register<JavaExec>("runCounter") {
    group = "application"
    description = "Run the counter example"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "org.tuava.examples.CounterApp"
    standardInput = System.`in`
}

// Task to run the todo example
tasks.register<JavaExec>("runTodo") {
    group = "application"
    description = "Run the todo example"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "org.tuava.examples.TodoApp"
    standardInput = System.`in`
}

// Ensure UTF-8 source encoding
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}