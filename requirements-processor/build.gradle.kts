plugins {
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.serialization") version "1.9.24"
    application
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("junit:junit:4.13.2")
}

application {
    mainClass.set("com.example.requirements.RequirementsProcessorCLI")
}

tasks.test {
    useJUnit()
}

kotlin {
    jvmToolchain(8)
}

// Create a JAR with all dependencies
tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.example.requirements.RequirementsProcessorCLI"
    }
    
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Task to run the demo
tasks.register<JavaExec>("demo") {
    group = "application"
    description = "Run the requirements processor demo"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.example.requirements.RequirementsDemo")
}

// Task to run tests
tasks.register<JavaExec>("test-requirements") {
    group = "verification"
    description = "Run requirements processor tests"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.example.requirements.RequirementsTest")
}