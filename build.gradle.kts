import org.jetbrains.intellij.tasks.PatchPluginXmlTask

plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
    id("org.jetbrains.intellij") version "1.17.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
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

    // Testing dependencies
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    
    // Detekt formatting
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.6")
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

// Detekt configuration
detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$projectDir/config/detekt/detekt-config.yml")
    baseline = file("$projectDir/config/detekt/baseline.xml")
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)  // Enable text report
        sarif.required.set(true)
    }
    
    // Print report to console after Detekt runs
    doLast {
        val reportsDir = layout.buildDirectory.dir("reports/detekt").get().asFile
        val xmlReportFile = File(reportsDir, "detekt.xml")
        
        // Parse and print violations from XML report
        if (xmlReportFile.exists()) {
            val xmlContent = xmlReportFile.readText()
            val errorPattern = """<error line="(\d+)" column="(\d+)" severity="[^"]*" message="([^"]*)" source="([^"]*)"/>""".toRegex()
            val filePattern = """<file name="([^"]*)">""".toRegex()
            
            val violations = mutableListOf<String>()
            var currentFile = ""
            
            xmlContent.lines().forEach { line ->
                filePattern.find(line)?.let { 
                    currentFile = it.groupValues[1] 
                }
                errorPattern.find(line)?.let { match ->
                    val lineNum = match.groupValues[1]
                    val column = match.groupValues[2]
                    val message = match.groupValues[3]
                    val rule = match.groupValues[4].substringAfterLast('.')
                    violations.add("$currentFile:$lineNum:$column: $message [$rule]")
                }
            }
            
            if (violations.isNotEmpty()) {
                println("\n" + "=".repeat(80))
                println("⚠️  DETEKT FOUND ${violations.size} ISSUE(S)")
                println("=".repeat(80))
                violations.forEach { println(it) }
                println("=".repeat(80))
                println("Full HTML report: file://$reportsDir/detekt.html")
                println("=".repeat(80) + "\n")
            } else {
                println("\n" + "=".repeat(80))
                println("✅ DETEKT: No code quality issues found!")
                println("=".repeat(80) + "\n")
            }
        }
    }
}

// CI/CD tasks
tasks.register("verify") {
    group = "verification"
    description = "Runs all verification tasks (build, test, detekt)"
    dependsOn("build", "test", "detekt")
}