# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

LinkSaver is an Android app that lets the user save links and share them via QR code. Kotlin, Jetpack Compose (Material 3), single Gradle module `:app`, package `it.cantarell.linksaver`. `minSdk` 26, `compileSdk`/`targetSdk` 37, Java 11 target. Dependencies and plugin versions live in the version catalog `gradle/libs.versions.toml`, so add new libraries there and not inline in `app/build.gradle.kts`.

The project is still at the Android Studio template stage: `MainActivity` hosts a placeholder `Greeting` composable and `ui/theme/` holds the generated `LinkSaverTheme`. There is no persistence, navigation, DI, or QR library yet. When introducing these, pick one approach and record it here.

## Commands

```bash
./gradlew assembleDebug                      # build debug APK
./gradlew installDebug                       # install on connected device/emulator
./gradlew testDebugUnitTest                  # JVM unit tests (app/src/test)
./gradlew connectedDebugAndroidTest          # instrumented/Compose UI tests (app/src/androidTest), needs a device
./gradlew lintDebug                          # Android lint

# single test class / method
./gradlew testDebugUnitTest --tests "it.cantarell.linksaver.ExampleUnitTest"
./gradlew testDebugUnitTest --tests "it.cantarell.linksaver.ExampleUnitTest.addition_isCorrect"
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=it.cantarell.linksaver.ExampleInstrumentedTest
```

The Gradle configuration cache is enabled (`gradle.properties`), so build logic must stay configuration-cache compatible.

## Testing requirements

- All new code must be tested, with **at least 80% coverage**. SonarCloud enforces this on PRs.
- No coverage tooling is configured yet. Before the first feature PR, enable it in `app/build.gradle.kts` (`buildTypes.debug { enableUnitTestCoverage = true; enableAndroidTestCoverage = true }`, which gives `createDebugUnitTestCoverageReport` / `createDebugAndroidTestCoverageReport`) and make sure SonarCloud gets the JaCoCo XML report.
- Put logic in plain classes (ViewModels, repositories, parsers/validators) that JVM unit tests can cover. Keep composables thin and cover them with Compose UI tests in `androidTest`.

## Workflow (GitHub issues → branch → PR)

Development is tracked through issues at https://github.com/cantarell-light-12pt/LinkSaver. Use the GitHub MCP server or `gh` to read issues. There is a single developer.

1. When you start an issue, create a new branch from an up-to-date `main`, e.g. `git checkout main && git pull && git checkout -b <issue-number>-<short-slug>`.
2. Every commit message starts with the issue number in brackets, followed by a brief, descriptive summary: `[#12] Add QR code generation for saved links`.
3. When the issue is done, push the branch and **open a pull request into `main`**, referencing the issue (e.g. `Closes #12`).
4. **Never merge PRs, and never push directly to `main`.** The owner approves and merges personally after the SonarCloud SAST analysis triggered by the PR passes. If Sonar reports issues, fix them with more commits on the same branch.
