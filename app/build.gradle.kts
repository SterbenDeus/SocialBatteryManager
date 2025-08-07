import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") version "2.2.0"
    kotlin("kapt")
    id("org.jlleitschuh.gradle.ktlint") version "13.0.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp") version libs.versions.ksp.get()
    jacoco
}

// Ensure the version catalog is properly imported
val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

// Explicitly import the version catalog
val mainLibs = extensions.getByType<VersionCatalogsExtension>().named("mainLibs")

android {
    packaging {
        resources.excludes.add("kotlin/reflect/reflect.kotlin_builtins")
    }

    namespace = "com.example.socialbatterymanager"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.socialbatterymanager"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "1.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = System.getenv("RELEASE_STORE_FILE")?.let { file(it) }
            storePassword = System.getenv("RELEASE_STORE_PASSWORD")
            keyAlias = System.getenv("RELEASE_KEY_ALIAS")
            keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
}

dependencies {
    implementation(kotlin("stdlib", "2.2.0"))

    // Lottie
    implementation(mainLibs.findLibrary("lottie").get())

    // AndroidX libraries
    implementation(mainLibs.findLibrary("androidx-core-ktx").get())
    implementation(mainLibs.findLibrary("androidx-lifecycle-runtime-ktx").get())
    implementation(mainLibs.findLibrary("androidx-lifecycle-viewmodel-ktx").get())
    implementation(mainLibs.findLibrary("androidx-lifecycle-livedata-ktx").get())
    implementation(mainLibs.findLibrary("androidx-fragment-ktx").get())
    implementation(mainLibs.findLibrary("androidx-activity-ktx").get())
    implementation(mainLibs.findLibrary("androidx-activity-compose").get())
    implementation(mainLibs.findLibrary("androidx-appcompat").get())
    implementation(mainLibs.findLibrary("androidx-constraintlayout").get())
    implementation(mainLibs.findLibrary("androidx-work-runtime-ktx").get())
    implementation(mainLibs.findLibrary("androidx-datastore-preferences").get())
    implementation(mainLibs.findLibrary("androidx-viewpager2").get())
    implementation(mainLibs.findLibrary("androidx-biometric").get())
    implementation(mainLibs.findLibrary("androidx-security-crypto").get())
    implementation(mainLibs.findLibrary("androidx-sqlite-framework").get())
    implementation(mainLibs.findLibrary("androidx-concurrent-futures").get())
    implementation(mainLibs.findLibrary("material").get())
    implementation(mainLibs.findLibrary("cardview").get())

    // Dagger Hilt and Room
    implementation(mainLibs.findLibrary("hilt-android").get())
    kapt(mainLibs.findLibrary("hilt-compiler").get())
    implementation(mainLibs.findLibrary("hilt-navigation-fragment").get())
    implementation(mainLibs.findLibrary("hilt-work").get())
    kapt(mainLibs.findLibrary("androidx-hilt-compiler").get())
    implementation(mainLibs.findLibrary("room-runtime").get())
    implementation(mainLibs.findLibrary("room-ktx").get())
    kapt(mainLibs.findLibrary("room-compiler").get())
    implementation(mainLibs.findLibrary("navigation-fragment-ktx").get())
    implementation(mainLibs.findLibrary("navigation-ui-ktx").get())
    implementation(platform(mainLibs.findLibrary("compose-bom").get()))
    implementation(mainLibs.findLibrary("compose-ui").get())
    implementation(mainLibs.findLibrary("compose-ui-graphics").get())
    implementation(mainLibs.findLibrary("compose-ui-tooling-preview").get())
    implementation(mainLibs.findLibrary("compose-material3").get())
    implementation(mainLibs.findLibrary("play-services-fitness").get())
    implementation(mainLibs.findLibrary("play-services-auth").get())
    implementation(platform(mainLibs.findLibrary("firebase-bom").get()))
    implementation(mainLibs.findLibrary("firebase-firestore-ktx").get())
    implementation(mainLibs.findLibrary("firebase-auth-ktx").get())
    implementation(mainLibs.findLibrary("firebase-crashlytics-ktx").get())
    implementation(mainLibs.findLibrary("pdfbox").get())
    implementation(mainLibs.findLibrary("opencsv").get())
    debugImplementation(mainLibs.findLibrary("fragment-testing").get())

    // Test dependencies
    testImplementation(libs.findLibrary("junit").get())
    testImplementation(libs.findLibrary("core-testing").get())
    testImplementation(libs.findLibrary("kotlinx-coroutines-test").get())
    testImplementation(libs.findLibrary("mockk").get())
    testImplementation(libs.findLibrary("robolectric").get())
    testImplementation(libs.findLibrary("work-testing").get())
    androidTestImplementation(libs.findLibrary("androidx-test-junit").get())
    androidTestImplementation(libs.findLibrary("androidx-test-core").get())
    androidTestImplementation(libs.findLibrary("androidx-test-espresso").get())
    androidTestImplementation(platform(libs.findLibrary("compose-bom").get()))
    androidTestImplementation(libs.findLibrary("work-testing").get())
    androidTestImplementation(libs.findLibrary("mockk-android").get())
    debugImplementation(libs.findLibrary("fragment-testing").get())
    debugImplementation(libs.findLibrary("ui-tooling").get())
    debugImplementation(libs.findLibrary("ui-test-manifest").get())
    implementation(project(":requirements-processor"))
}

kapt {
    correctErrorTypes = true
}

ktlint {
    version.set("1.0.1")
    debug.set(true)
    verbose.set(true)
    android.set(true)
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(false)
    enableExperimentalRules.set(true)
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$projectDir/config/detekt/detekt.yml")
    baseline = file("$projectDir/config/detekt/baseline.xml")
}

kotlin {
    jvmToolchain(17)
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.withType<Test> {
    finalizedBy("jacocoTestReport")
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    classDirectories.setFrom(
        fileTree("${'$'}{buildDir}/tmp/kotlin-classes/debug") {
            exclude("**/R.class", "**/R${'$'}*.class", "**/BuildConfig.*", "**/Manifest*.*", "**/*Test*.*")
        },
    )
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(fileTree(buildDir) { include("**/jacoco/testDebugUnitTest.exec") })
}

tasks.register<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn("jacocoTestReport")
    classDirectories.setFrom(
        fileTree("${'$'}{buildDir}/tmp/kotlin-classes/debug") {
            exclude("**/R.class", "**/R${'$'}*.class", "**/BuildConfig.*", "**/Manifest*.*", "**/*Test*.*")
        },
    )
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(fileTree(buildDir) { include("**/jacoco/testDebugUnitTest.exec") })
    violationRules {
        rule {
            limit {
                minimum = "0.5".toBigDecimal()
            }
        }
    }
}
