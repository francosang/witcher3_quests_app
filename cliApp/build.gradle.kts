plugins {
    alias(libs.plugins.kotlinJvm)

    id("org.jetbrains.kotlinx.dataframe") version "0.15.0"
    kotlin("plugin.serialization") version "2.1.0"

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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")

    implementation("org.jetbrains.kotlinx:dataframe:0.15.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    implementation("org.apache.poi:poi:5.3.0")
    implementation("org.apache.poi:poi-ooxml:5.3.0")

    implementation(project(":shared"))
}

application {
    // Define the main class for the application.
    mainClass = "com.jfranco.witcher3.quests.cli.MainKt"
}
