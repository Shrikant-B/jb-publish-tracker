import org.jetbrains.intellij.tasks.PatchPluginXmlTask

plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
    id("org.jetbrains.intellij") version "1.17.0"
}

group = "com.shrikantbadwaik"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-client-okhttp:2.3.12")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    // Use kotlinx-coroutines if you use structured concurrency across code (not required for simple usage)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // Chart library for visualizations
    implementation("org.jfree:jfreechart:1.5.4")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

intellij {
    // change to the IntelliJ/Android Studio version you target
    version.set("2023.3")
    plugins.set(listOf("java"))
}

tasks {
    // Optional: patch plugin.xml version from Gradle
    withType<PatchPluginXmlTask> {
        changeNotes.set("Initial release")
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}