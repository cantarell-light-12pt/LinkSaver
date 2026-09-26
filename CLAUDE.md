# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

LinkSaver is an Android app that lets the user save links and share them via QR code. Kotlin, Jetpack Compose (Material 3), single Gradle module `:app`, package `it.cantarell.linksaver`. `minSdk` 26, `compileSdk`/`targetSdk` 37, Java 11 target. Dependencies and plugin versions live in the version catalog `gradle/libs.versions.toml`, so add new libraries there and not inline in `app/build.gradle.kts`. Gradle dependency verification is on (`gradle/verification-metadata.xml`, SHA-256 checksums): after adding or upgrading a dependency or plugin, run `./gradlew --write-verification-metadata sha256 help assembleRelease assembleDebugAndroidTest testDebugUnitTest` and commit the updated file, or the build fails verification.

### Architecture decisions

- **Persistence**: Room (`data/LinkDatabase`, entity `Link`, `LinkDao`), code generated with KSP. The schema is exported to `app/schemas/`, so commit the new JSON whenever the schema version changes. Tags are stored in a single column via `Converters`.
- **DI**: manual. `LinkSaverApplication` owns an `AppContainer` that builds the database and repositories lazily. ViewModels get their dependencies through a `viewModelFactory` in their companion (`Factory`).
- **UI state**: one ViewModel per screen exposing a `StateFlow<…UiState>` and a single `onEvent(…Event)` entry point. `…Route` composables collect the state; `…Screen` composables are stateless and take `state` + `onEvent`.
- **Validation**: pure Kotlin in `data/LinkInputValidator` (JVM-testable, no Android APIs).
- **Navigation / QR**: not introduced yet. `MainActivity` shows the add-link screen directly.

## Commands

```bash
./gradlew assembleDebug                      # build debug APK
./gradlew installDebug                       # install on connected device/emulator
./gradlew testDebugUnitTest                  # JVM unit tests (app/src/test)
./gradlew connectedDebugAndroidTest          # instrumented/Compose UI tests (app/src/androidTest), needs a device
./gradlew lintDebug                          # Android lint
./gradlew sonar                              # SonarCloud analysis (needs SONAR_TOKEN; normally run by CI)

# single test class / method
./gradlew testDebugUnitTest --tests "it.cantarell.linksaver.ExampleUnitTest"
./gradlew testDebugUnitTest --tests "it.cantarell.linksaver.ExampleUnitTest.addition_isCorrect"
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=it.cantarell.linksaver.ExampleInstrumentedTest
```

The Gradle configuration cache is enabled (`gradle.properties`), so build logic must stay configuration-cache compatible.

## Testing requirements

- All new code must be tested, with **at least 80% coverage**. SonarCloud enforces this on PRs.
- Coverage is enabled on the debug build type. `./gradlew createDebugUnitTestCoverageReport` writes `app/build/reports/coverage/test/debug/report.xml`, and `./gradlew createDebugAndroidTestCoverageReport` (runs the instrumented tests, needs a device) writes `app/build/reports/coverage/androidTest/debug/connected/report.xml`. SonarCloud must receive both JaCoCo XML reports.
- JaCoCo comes from AGP's built-in coverage, with its version pinned in the catalog (`jacoco`) through `android.testCoverage`. The `org.sonarqube` Gradle plugin, applied in the root build, sends both reports. Its module settings (report paths, exclusions) live in `app/build.gradle.kts`. Don't set `sonar.sources`/`sonar.tests` there: the plugin adds the detected Android sources and tests to them when `sonar` runs, and overlapping paths make the scanner fail with "can't be indexed twice".
- CI (`.github/workflows/build.yml`) runs on PRs and on pushes to `main`. It runs both coverage tasks (the instrumented ones on an emulator) and then `./gradlew sonar`. This replaces SonarCloud Automatic Analysis, which cannot import coverage.
- Put logic in plain classes (ViewModels, repositories, parsers/validators) that JVM unit tests can cover. Keep composables thin and cover them with Compose UI tests in `androidTest`. Room DAOs are tested in `androidTest` against an in-memory database.

## Workflow (GitHub issues → branch → PR)

Development is tracked through issues at https://github.com/cantarell-light-12pt/LinkSaver. Interact with GitHub **exclusively through the GitHub MCP server** (issues, branches, pushes, PRs); `gh` and git HTTPS pushes are not used. There is a single developer.

1. When you start an issue, create a new branch from an up-to-date `main`, e.g. `git checkout main && git pull && git checkout -b <issue-number>-<short-slug>`.
2. Every commit message starts with the issue number in brackets, followed by a brief, descriptive summary: `[#12] Add QR code generation for saved links`.
3. When the issue is done, push the branch and **open a pull request into `main`**, referencing the issue (e.g. `Closes #12`).
4. **Never merge PRs, and never push directly to `main`.** The owner approves and merges personally after the SonarCloud SAST analysis triggered by the PR passes. If Sonar reports issues, fix them with more commits on the same branch.
