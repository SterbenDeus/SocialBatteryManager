plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.0"
    application
}

group = "com.example"
version = "1.0.0"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    testImplementation("org.jetbrains.kotlin:kotlin-test:2.2.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.2.0")
    testImplementation("junit:junit:4.13.2")
}

application {
    mainClass.set("com.example.requirements.RequirementsProcessorCLI")
}

tasks.test {
    useJUnit()
}

kotlin {
    jvmToolchain(17)
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.example.requirements.RequirementsProcessorCLI"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<JavaExec>("demo") {
    group = "application"
    description = "Run the requirements processor demo"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.example.requirements.RequirementsDemo")
}

tasks.register<JavaExec>("test-requirements") {
    group = "verification"
    description = "Run requirements processor tests"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.example.requirements.RequirementsTest")
}