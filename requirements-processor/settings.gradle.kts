pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "requirements-processor"

dependencyResolutionManagement {
    versionCatalogs {
        create("processorLibs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
