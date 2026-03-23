package edu.hitsz;

import android.app.Activity;
import android.app.Instrumentation;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class MainActivityNavigationTest {

    @Test
    public void easyButtonLaunchesGameActivityWithEasyDifficulty() {
        assertLaunchesDifficulty(R.id.button_easy, GameDifficulty.EASY);
    }

    @Test
    public void normalButtonLaunchesGameActivityWithNormalDifficulty() {
        assertLaunchesDifficulty(R.id.button_normal, GameDifficulty.NORMAL);
    }

    @Test
    public void hardButtonLaunchesGameActivityWithHardDifficulty() {
        assertLaunchesDifficulty(R.id.button_hard, GameDifficulty.HARD);
    }

    private void assertLaunchesDifficulty(int buttonId, String expectedDifficulty) {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(GameActivity.class.getName(), null, false);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(buttonId)).perform(ViewActions.click());

            Activity activity = instrumentation.waitForMonitorWithTimeout(monitor, 5000);
            assertNotNull(activity);
            assertEquals(expectedDifficulty, activity.getIntent().getStringExtra(GameDifficulty.EXTRA_DIFFICULTY));
            activity.finish();
        } finally {
            instrumentation.removeMonitor(monitor);
        }
    }
}
