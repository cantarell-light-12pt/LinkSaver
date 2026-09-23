plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android {
    namespace = "it.cantarell.linksaver"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "it.cantarell.linksaver"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    testCoverage {
        jacocoVersion = libs.versions.jacoco.get()
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

// The Sonar plugin does not detect source sets with AGP's built-in Kotlin, so they are
// listed explicitly. Coverage comes from the JaCoCo XML reports produced by
// createDebugUnitTestCoverageReport and createDebugAndroidTestCoverageReport.
configure<org.sonarqube.gradle.SonarExtension> {
    properties {
        property("sonar.sources", "src/main")
        property("sonar.tests", "src/test,src/androidTest")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            listOf(
                "build/reports/coverage/test/debug/report.xml",
                "build/reports/coverage/androidTest/debug/connected/report.xml",
            ).joinToString(","),
        )
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}