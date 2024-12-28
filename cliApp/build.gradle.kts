plugins {
    alias(libs.plugins.kotlinJvm)

    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":shared"))
}

application {
    // Define the main class for the application.
    mainClass = "com.jfranco.witcher3.quests.cli.MainKt"
}
