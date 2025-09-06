import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("org.jlleitschuh.gradle.ktlint") version libs.versions.ktlint.get()
    id("io.gitlab.arturbosch.detekt") version libs.versions.detekt.get()
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    jacoco
}

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

        buildConfigField("Boolean", "FEATURE_CONTACTS_IMPORT", "true")
        buildConfigField("Boolean", "FEATURE_CALENDAR_IMPORT", "true")
        buildConfigField("Boolean", "FEATURE_NOTIFICATIONS", "true")
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
    // Lottie
    implementation(libs.lottie)

    // AndroidX libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.sqlite.framework)
    implementation(libs.material)
    implementation(libs.cardview)

    // Dagger Hilt and Room
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.fragment)
    implementation(libs.hilt.work)
    kapt(libs.androidx.hilt.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.play.services.fitness)
    implementation(libs.play.services.auth)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.pdfbox)
    implementation(libs.opencsv)
    debugImplementation(libs.fragment.testing)

    // Test dependencies
    testImplementation(libs.junit)
    testImplementation(libs.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)
    testImplementation(libs.work.testing)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.espresso)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.work.testing)
    androidTestImplementation(libs.mockk.android)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
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
