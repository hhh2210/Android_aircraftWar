# AGENTS.md

Repository guidance for coding agents working in `Android_aircraftWar`.

This project is an Android app written in Java with Gradle Groovy DSL.
The active app module is `:app`.
IDE: Google Android Studio. This is a two-person team project.

## Scope and Current State

- The repository contains an Android migration of a Windows aircraft-war game.
- The Android app entry point is `app/src/main/java/edu/hitsz/MainActivity.java`.
- The main runtime surface is `app/src/main/java/edu/hitsz/application/BaseGame.java`.
- Game assets live under `app/src/main/res/drawable`.
- Core game logic is organized by package: `aircraft`, `bullet`, `prop`, `factory`, `strategy`, `observer`, `application`, `basic`.
- There is also a legacy `AircraftWar-win/` directory with the original Windows code; do not treat it as part of the Android runtime unless the task explicitly involves migration/reference work.

## Build Commands

Run all commands from the repository root:

```bash
./gradlew assembleDebug
```

- Builds the debug APK for the Android app.
- Use this as the default verification command after code changes.

```bash
./gradlew build
```

- Runs the standard Gradle lifecycle for the app module.
- Includes unit tests and packaging tasks.

```bash
./gradlew clean
```

- Removes Gradle build outputs when the build state looks stale.
- Do not run it unless needed; it slows iteration.

## Lint Commands

```bash
./gradlew lint
```

- Runs Android lint for all variants.

```bash
./gradlew lintDebug
```

- Faster variant-specific lint run for most day-to-day work.

## Test Commands

Local JVM unit tests:

```bash
./gradlew testDebugUnitTest
```

- Runs local unit tests in `app/src/test/java`.

Single local JVM test class:

```bash
./gradlew testDebugUnitTest --tests "edu.hitsz.ExampleUnitTest"
```

Single local JVM test method:

```bash
./gradlew testDebugUnitTest --tests "edu.hitsz.ExampleUnitTest.addition_isCorrect"
```

Instrumented Android tests (requires emulator or device):

```bash
./gradlew connectedDebugAndroidTest
```

- Runs tests in `app/src/androidTest/java` on a connected emulator/device.

Single instrumented test class:

```bash
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=edu.hitsz.ExampleInstrumentedTest
```

Single instrumented test method:

```bash
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=edu.hitsz.ExampleInstrumentedTest#useAppContext
```

## Useful Android Workflow Commands

```bash
./gradlew installDebug
```

- Installs the debug build onto a connected device/emulator.

```bash
./gradlew tasks
```

- Lists available Gradle tasks if you need to discover module-specific commands.

## Project Conventions

### Language and Platform

- Use Java, not Kotlin, unless the user explicitly asks for Kotlin migration.
- Match the current Android SDK setup in `app/build.gradle`.
- Current Java compatibility target is Java 11.
- Keep Android-specific code in the Android app module; do not pull desktop Java APIs into app code.

### Imports

- Use normal explicit imports; avoid wildcard imports.
- Group imports in the existing style: Android/AndroidX imports first, then Java standard library imports, then project imports.
- Remove unused imports after edits.
- Do not import desktop-only packages such as `java.awt`, `javax.swing`, `javax.sound`, or `javax.imageio` into Android code.

### Formatting

- Follow the existing Java style already present in the repo.
- Use 4 spaces for indentation.
- Keep braces on the same line as declarations and control statements.
- Preserve current spacing conventions even if they are not fully Google-style.
- Keep methods reasonably short when possible, but prioritize readability over forced extraction.
- Avoid reformatting unrelated files.

### Naming

- Classes: PascalCase, e.g. `BaseGame`, `HeroAircraft`, `ImageManager`.
- Methods and fields: camelCase, e.g. `spawnEnemies`, `cycleDuration`, `floatingNotice`.
- Constants: UPPER_SNAKE_CASE, e.g. `FRAME_INTERVAL`, `PROP_EFFECT_DURATION`.
- Packages stay lowercase and feature-oriented, e.g. `edu.hitsz.strategy`.
- Resource names use lowercase with underscores, e.g. `elite_plus.png`, `prop_bullet_plus.png`.

### Types and Data Modeling

- Prefer primitive types (`int`, `boolean`, `long`) for frame timing, coordinates, HP, counters, and flags, matching the existing codebase.
- Use `List<T>` for interfaces and `LinkedList<>` where the code already uses it for active entity collections.
- Keep factory, strategy, and observer abstractions intact; new gameplay behavior should usually fit into those existing extension points.
- Do not replace the current architecture with MVVM, Room, Retrofit, etc. unless the task explicitly asks for that larger redesign.

### Error Handling

- Fail safely in runtime paths that interact with threads, surfaces, or Android resources.
- For interrupt handling, preserve the interrupt status with `Thread.currentThread().interrupt()`.
- Prefer guard clauses for null/invalid state checks.
- Avoid swallowing exceptions silently unless there is a clear reason.
- Avoid introducing crash-prone assumptions around `SurfaceHolder`, `Canvas`, or uninitialized singletons.
- If a method can return early on invalid state, do that instead of adding deep nesting.

### Android-Specific Guidance

- `BaseGame` owns the rendering loop; changes to frame timing, drawing, or touch behavior should be tested carefully.
- `ImageManager` is the central bitmap loader; add new art assets there rather than decoding resources ad hoc throughout the codebase.
- Keep game rendering off the UI thread when working in `SurfaceView` code.
- Any new resource added to `res/drawable` must use Android-safe lowercase underscore naming.
- Be careful with lifecycle-sensitive code in `surfaceCreated`, `surfaceChanged`, and `surfaceDestroyed`.

### Testing Expectations

- For logic-only changes, run at least `./gradlew testDebugUnitTest`.
- For rendering or Android lifecycle changes, prefer `./gradlew assembleDebug` plus an emulator smoke test.
- If you add a pure logic class, consider adding a local JVM test under `app/src/test/java`.
- If you change Android framework behavior, add or update an instrumented test only when it is practical.

### Files to Treat Carefully

- `app/src/main/java/edu/hitsz/application/BaseGame.java`: central game loop, drawing, collision, spawning.
- `app/src/main/java/edu/hitsz/application/ImageManager.java`: global bitmap initialization and mapping.
- `app/src/main/java/edu/hitsz/aircraft/HeroAircraft.java`: singleton lifecycle and timed prop effects.
- `app/src/main/java/edu/hitsz/basic/AbstractFlyingObject.java`: shared position, collision, and image sizing logic.

## What Agents Should Usually Do

- Read the relevant package first and follow the established gameplay patterns.
- Make the smallest safe change that satisfies the task.
- Verify with Gradle before declaring completion.
- Mention clearly if a change is only build-verified and not emulator-verified.

## What Agents Should Avoid

- Do not delete or rewrite large portions of migrated game logic without a clear request.
- Do not add heavy new dependencies casually.
- Do not move the project to Kotlin, Compose, or a new architecture as a side quest.
- Do not treat `AircraftWar-win/` as an Android source root.
- Do not commit generated build outputs or local IDE state.
