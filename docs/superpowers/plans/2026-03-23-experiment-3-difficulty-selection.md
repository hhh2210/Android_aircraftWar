# Experiment 3 Difficulty Selection Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an XML-based difficulty selection page that launches a shared `GameActivity` via `Intent` extras and loads Easy, Normal, or Hard gameplay based on the selected difficulty.

**Architecture:** Keep `MainActivity` as the XML landing page and add a thin `GameActivity` that programmatically mounts the existing `BaseGame`-derived view for the requested difficulty. Introduce one tiny pure-Java difficulty helper so the routing contract can be covered by local JVM tests, then use instrumented tests for the Activity UI and game-view routing.

**Tech Stack:** Java 11, Android AppCompat, ConstraintLayout, JUnit4, AndroidX Instrumentation, Espresso, Gradle Groovy DSL

**Git note:** Do not create commits during execution unless the user explicitly asks for one.

---

## File Structure

- Create: `app/src/main/java/edu/hitsz/GameDifficulty.java` - shared extra key, supported values, and fallback normalization.
- Modify: `app/src/main/java/edu/hitsz/MainActivity.java` - swap the direct `EasyGame` launch for XML UI wiring and button listeners.
- Create: `app/src/main/java/edu/hitsz/GameActivity.java` - receive the selected difficulty and mount the matching game view.
- Create: `app/src/main/java/edu/hitsz/application/NormalGame.java` - lightweight wrapper for `NormalMode`.
- Create: `app/src/main/java/edu/hitsz/application/HardGame.java` - lightweight wrapper for `HardMode`.
- Modify: `app/src/main/AndroidManifest.xml` - register `GameActivity`.
- Modify: `app/src/main/res/layout/activity_main.xml` - replace the template layout with the difficulty selection screen.
- Modify: `app/src/main/res/values/strings.xml` - move title, subtitle, and button labels into Android resources.
- Create: `app/src/test/java/edu/hitsz/GameDifficultyTest.java` - JVM test coverage for difficulty normalization.
- Create: `app/src/androidTest/java/edu/hitsz/MainActivityScreenTest.java` - verifies the landing screen content renders.
- Create: `app/src/androidTest/java/edu/hitsz/GameActivityTest.java` - verifies `GameActivity` mounts the expected view for each difficulty and falls back safely.
- Create: `app/src/androidTest/java/edu/hitsz/MainActivityNavigationTest.java` - verifies button clicks launch `GameActivity` with the correct extra.

## Execution Notes

- Start or connect an emulator before running any `connectedDebugAndroidTest` commands.
- Keep `GameActivity` programmatic; do not add a separate game-page XML layout.
- Reuse the existing `EasyGame` pattern for `NormalGame` and `HardGame`; do not modify `BaseGame` for this experiment.

### Task 1: Lock the difficulty contract behind a JVM test

**Files:**
- Create: `app/src/test/java/edu/hitsz/GameDifficultyTest.java`
- Create: `app/src/main/java/edu/hitsz/GameDifficulty.java`

- [ ] **Step 1: Write the failing unit test**

```java
package edu.hitsz;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GameDifficultyTest {

    @Test
    public void normalize_keepsSupportedValues() {
        assertEquals(GameDifficulty.EASY, GameDifficulty.normalize(GameDifficulty.EASY));
        assertEquals(GameDifficulty.NORMAL, GameDifficulty.normalize(GameDifficulty.NORMAL));
        assertEquals(GameDifficulty.HARD, GameDifficulty.normalize(GameDifficulty.HARD));
    }

    @Test
    public void normalize_fallsBackToEasyForNull() {
        assertEquals(GameDifficulty.EASY, GameDifficulty.normalize(null));
    }

    @Test
    public void normalize_fallsBackToEasyForUnknownValue() {
        assertEquals(GameDifficulty.EASY, GameDifficulty.normalize("boss"));
    }
}
```

- [ ] **Step 2: Run the test to verify RED**

Run: `./gradlew testDebugUnitTest --tests "edu.hitsz.GameDifficultyTest"`
Expected: FAIL with a compilation error because `GameDifficulty` does not exist yet.

- [ ] **Step 3: Write the minimal production code**

```java
package edu.hitsz;

public final class GameDifficulty {

    public static final String EXTRA_DIFFICULTY = "edu.hitsz.extra.DIFFICULTY";
    public static final String EASY = "easy";
    public static final String NORMAL = "normal";
    public static final String HARD = "hard";

    private GameDifficulty() {
    }

    public static String normalize(String difficulty) {
        if (NORMAL.equals(difficulty)) {
            return NORMAL;
        }
        if (HARD.equals(difficulty)) {
            return HARD;
        }
        return EASY;
    }
}
```

- [ ] **Step 4: Run the unit test to verify GREEN**

Run: `./gradlew testDebugUnitTest --tests "edu.hitsz.GameDifficultyTest"`
Expected: PASS for all three normalization cases.

- [ ] **Step 5: Leave the worktree uncommitted**

Do not create a git commit unless the user explicitly asks for one.

### Task 2: Build the XML difficulty selection screen

**Files:**
- Create: `app/src/androidTest/java/edu/hitsz/MainActivityScreenTest.java`
- Modify: `app/src/main/java/edu/hitsz/MainActivity.java`
- Modify: `app/src/main/res/layout/activity_main.xml`
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: Write the failing instrumented UI test**

```java
package edu.hitsz;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class MainActivityScreenTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void showsDifficultySelectionContent() {
        onView(withId(R.id.text_title)).check(matches(withText(R.string.main_title)));
        onView(withId(R.id.text_subtitle)).check(matches(withText(R.string.main_subtitle)));
        onView(withId(R.id.button_easy)).check(matches(isDisplayed()));
        onView(withId(R.id.button_normal)).check(matches(isDisplayed()));
        onView(withId(R.id.button_hard)).check(matches(isDisplayed()));
    }
}
```

- [ ] **Step 2: Run the test to verify RED**

Run: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=edu.hitsz.MainActivityScreenTest`
Expected: FAIL because `MainActivity` still shows `EasyGame` directly and the new view IDs/resources are missing.

- [ ] **Step 3: Replace the template layout and strings, then inflate the XML screen from `MainActivity`**

```xml
<!-- app/src/main/res/layout/activity_main.xml -->
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="24dp">

    <TextView
        android:id="@+id/text_title"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:gravity="center"
        android:text="@string/main_title"
        android:textSize="32sp"
        android:textStyle="bold"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <TextView
        android:id="@+id/text_subtitle"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:gravity="center"
        android:text="@string/main_subtitle"
        android:textSize="18sp"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/text_title" />

    <Button
        android:id="@+id/button_easy"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginTop="48dp"
        android:text="@string/difficulty_easy"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/text_subtitle" />

    <Button
        android:id="@+id/button_normal"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:text="@string/difficulty_normal"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/button_easy" />

    <Button
        android:id="@+id/button_hard"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:text="@string/difficulty_hard"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/button_normal" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

```xml
<!-- app/src/main/res/values/strings.xml -->
<resources>
    <string name="app_name">AircraftWar</string>
    <string name="main_title">Aircraft War</string>
    <string name="main_subtitle">Select a difficulty to start the battle</string>
    <string name="difficulty_easy">Easy</string>
    <string name="difficulty_normal">Normal</string>
    <string name="difficulty_hard">Hard</string>
</resources>
```

```java
// app/src/main/java/edu/hitsz/MainActivity.java
package edu.hitsz;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
```

- [ ] **Step 4: Run the instrumented test to verify GREEN**

Run: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=edu.hitsz.MainActivityScreenTest`
Expected: PASS, with all title/subtitle/button assertions succeeding because `MainActivity` now inflates the XML layout.

- [ ] **Step 5: Keep the screen work uncommitted**

Do not create a git commit unless the user explicitly asks for one.

### Task 3: Add shared game routing and wrapper views

**Files:**
- Create: `app/src/androidTest/java/edu/hitsz/GameActivityTest.java`
- Create: `app/src/main/java/edu/hitsz/GameActivity.java`
- Create: `app/src/main/java/edu/hitsz/application/NormalGame.java`
- Create: `app/src/main/java/edu/hitsz/application/HardGame.java`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Write the failing routing test for `GameActivity`**

```java
package edu.hitsz;

import android.content.Intent;
import android.app.Activity;
import android.app.Instrumentation;
import android.view.ViewGroup;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class GameActivityTest {

    @Test
    public void loadsNormalGameWhenNormalDifficultyIsRequested() {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), GameActivity.class);
        intent.putExtra(GameDifficulty.EXTRA_DIFFICULTY, GameDifficulty.NORMAL);
        assertRootView(intent, "NormalGame");
    }

    @Test
    public void loadsHardGameWhenHardDifficultyIsRequested() {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), GameActivity.class);
        intent.putExtra(GameDifficulty.EXTRA_DIFFICULTY, GameDifficulty.HARD);
        assertRootView(intent, "HardGame");
    }

    @Test
    public void fallsBackToEasyGameWhenDifficultyIsMissingOrInvalid() {
        Intent missing = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), GameActivity.class);
        assertRootView(missing, "EasyGame");

        Intent invalid = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), GameActivity.class);
        invalid.putExtra(GameDifficulty.EXTRA_DIFFICULTY, "invalid");
        assertRootView(invalid, "EasyGame");
    }

    private void assertRootView(Intent intent, String expectedSimpleName) {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Activity activity = instrumentation.startActivitySync(intent);
        instrumentation.waitForIdleSync();

        try {
            ViewGroup content = activity.findViewById(android.R.id.content);
            assertEquals(expectedSimpleName, content.getChildAt(0).getClass().getSimpleName());
        } finally {
            activity.finish();
        }
    }
}
```

- [ ] **Step 2: Run the test to verify RED**

Run: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=edu.hitsz.GameActivityTest`
Expected: FAIL with compilation errors because `GameActivity`, `NormalGame`, and `HardGame` do not exist yet.

- [ ] **Step 3: Implement the routing Activity, wrappers, and manifest entry**

```java
// app/src/main/java/edu/hitsz/application/NormalGame.java
package edu.hitsz.application;

import android.content.Context;

import edu.hitsz.application.gamemode.NormalMode;

public class NormalGame extends BaseGame {

    public NormalGame(Context context) {
        super(context, new NormalMode());
    }
}
```

```java
// app/src/main/java/edu/hitsz/application/HardGame.java
package edu.hitsz.application;

import android.content.Context;

import edu.hitsz.application.gamemode.HardMode;

public class HardGame extends BaseGame {

    public HardGame(Context context) {
        super(context, new HardMode());
    }
}
```

```java
// app/src/main/java/edu/hitsz/GameActivity.java
package edu.hitsz;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.application.EasyGame;
import edu.hitsz.application.HardGame;
import edu.hitsz.application.NormalGame;

public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String difficulty = GameDifficulty.normalize(
                getIntent().getStringExtra(GameDifficulty.EXTRA_DIFFICULTY)
        );
        setContentView(createGameViewByDifficulty(difficulty));
    }

    private View createGameViewByDifficulty(String difficulty) {
        if (GameDifficulty.NORMAL.equals(difficulty)) {
            return new NormalGame(this);
        }
        if (GameDifficulty.HARD.equals(difficulty)) {
            return new HardGame(this);
        }
        return new EasyGame(this);
    }
}
```

```xml
<!-- app/src/main/AndroidManifest.xml -->
<activity android:name=".GameActivity" android:exported="false" />
```

- [ ] **Step 4: Run the routing test to verify GREEN**

Run: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=edu.hitsz.GameActivityTest`
Expected: PASS, showing correct view selection for `normal`, `hard`, and the easy fallback.

- [ ] **Step 5: Keep the routing work uncommitted**

Do not create a git commit unless the user explicitly asks for one.

### Task 4: Wire `MainActivity` button clicks to `GameActivity`

**Files:**
- Create: `app/src/androidTest/java/edu/hitsz/MainActivityNavigationTest.java`
- Modify: `app/src/main/java/edu/hitsz/MainActivity.java`

- [ ] **Step 1: Write the failing button-navigation test**

```java
package edu.hitsz;

import android.app.Activity;
import android.app.Instrumentation;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class MainActivityNavigationTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void easyNormalAndHardButtonsLaunchGameActivityWithMatchingExtras() {
        assertLaunch(R.id.button_easy, GameDifficulty.EASY);
        assertLaunch(R.id.button_normal, GameDifficulty.NORMAL);
        assertLaunch(R.id.button_hard, GameDifficulty.HARD);
    }

    private void assertLaunch(int buttonId, String expectedDifficulty) {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Instrumentation.ActivityMonitor monitor =
                instrumentation.addMonitor(GameActivity.class.getName(), null, false);

        try {
            onView(withId(buttonId)).perform(click());
            Activity launched = instrumentation.waitForMonitorWithTimeout(monitor, 3000);
            assertNotNull(launched);
            assertEquals(expectedDifficulty,
                    launched.getIntent().getStringExtra(GameDifficulty.EXTRA_DIFFICULTY));
            launched.finish();
        } finally {
            instrumentation.removeMonitor(monitor);
        }
    }
}
```

- [ ] **Step 2: Run the test to verify RED**

Run: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=edu.hitsz.MainActivityNavigationTest`
Expected: FAIL because the XML buttons exist, but `MainActivity` does not attach click listeners yet.

- [ ] **Step 3: Replace the direct game launch with XML-driven button listeners**

```java
package edu.hitsz;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_easy).setOnClickListener(view -> launchGame(GameDifficulty.EASY));
        findViewById(R.id.button_normal).setOnClickListener(view -> launchGame(GameDifficulty.NORMAL));
        findViewById(R.id.button_hard).setOnClickListener(view -> launchGame(GameDifficulty.HARD));
    }

    private void launchGame(String difficulty) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameDifficulty.EXTRA_DIFFICULTY, difficulty);
        startActivity(intent);
    }
}
```

- [ ] **Step 4: Run the navigation and screen tests to verify GREEN**

Run: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=edu.hitsz.MainActivityNavigationTest`
Expected: PASS for all three button launches.

Run: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=edu.hitsz.MainActivityScreenTest`
Expected: PASS now that `MainActivity` actually inflates the XML layout.

- [ ] **Step 5: Keep the Activity wiring uncommitted**

Do not create a git commit unless the user explicitly asks for one.

### Task 5: Run the full verification sweep

**Files:**
- No file edits.

- [ ] **Step 1: Re-run all local JVM tests**

Run: `./gradlew testDebugUnitTest`
Expected: PASS, including `edu.hitsz.GameDifficultyTest` and the existing example unit test.

- [ ] **Step 2: Re-run the experiment-specific instrumented tests**

Run: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=edu.hitsz.MainActivityScreenTest,edu.hitsz.GameActivityTest,edu.hitsz.MainActivityNavigationTest`
Expected: PASS for all three experiment-specific instrumented test classes.

- [ ] **Step 3: Build the debug APK**

Run: `./gradlew assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Perform a manual smoke test on the emulator/device**

Verify this exact path manually:

1. Launch the app and confirm the difficulty selection page appears first.
2. Tap `Easy` and confirm gameplay starts.
3. Press Back and confirm the selection page returns.
4. Repeat for `Normal` and `Hard`.
5. Confirm no launch crash occurs if `GameActivity` is reopened from a stale/invalid intent during testing.

- [ ] **Step 5: Stop and report results without committing**

Summarize test/build/manual verification results and do not create a git commit unless the user explicitly asks for one.
