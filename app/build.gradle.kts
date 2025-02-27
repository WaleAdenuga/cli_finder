/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin application project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.12/userguide/building_java_projects.html in the Gradle documentation.
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    kotlin("jvm") version "2.1.0"

    // Apply the application plugin to add support for building a CLI application in Java.
    // Simplifies specifying the entry point for the program
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // This dependency is used by the application.
    implementation(libs.guava)
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use JUnit Jupiter test framework
            useJUnitJupiter("5.11.1")
        }
    }
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
}

application {
    // Define the main class for the application.
    // Kotlin automatically appends "Kt" to the file containing the main function
    mainClass.set("FileFinderKt")
}

tasks.named<JavaExec>("run") {
    args = listOf(".", "app", "--true", "--shallow") // Replace with the arguments you want
}

// customizes the behaviour of the jar task gradle uses to package the application
tasks.jar {
    manifest {
        // additional metadata - tells the jvm which class contains the main function
        attributes(
            // dynamically retrieves the mainClass set in the application block and adds it to the MANIFEST.MF file
            "Main-Class" to application.mainClass.get()
        )
    }
    // Add all runtime dependencies into the JAR for a runnable JAR
    from({
        // configurations.runtimeClasspath.get() gets all runtime dependencies for the project
        // map iterates through all dependencies, unpacking any jar files (via ziptree) and including their contents in the final JAR
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })

    // set a duplicate file handling strategy
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // Set where jar file is built
    archiveBaseName.set("cli_finder")
    archiveVersion.set("1.0.0")
    archiveClassifier.set("")

    destinationDirectory.set(file("C:\\Tools\\cli_finder"))
}