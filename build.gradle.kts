// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.sonarqube)
}

sonar {
    properties {
        property("sonar.projectKey", "cantarell-light-12pt_LinkSaver")
        property("sonar.organization", "andrewkant-ml")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}