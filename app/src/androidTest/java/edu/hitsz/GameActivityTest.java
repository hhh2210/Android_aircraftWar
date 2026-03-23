package edu.hitsz;

import android.app.Activity;
import android.content.Intent;
import android.view.ViewGroup;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class GameActivityTest {

    @Test
    public void loadsNormalGameForNormalDifficulty() {
        assertRootChildSimpleName(GameDifficulty.NORMAL, "NormalGame");
    }

    @Test
    public void loadsHardGameForHardDifficulty() {
        assertRootChildSimpleName(GameDifficulty.HARD, "HardGame");
    }

    @Test
    public void fallsBackToEasyGameForMissingOrInvalidDifficulty() {
        assertRootChildSimpleName(null, "EasyGame");
        assertRootChildSimpleName("invalid", "EasyGame");
    }

    private void assertRootChildSimpleName(String difficulty, String expectedSimpleName) {
        Intent intent = new Intent()
                .setClassName(
                        InstrumentationRegistry.getInstrumentation().getTargetContext(),
                        "edu.hitsz.GameActivity"
                );

        if (difficulty != null) {
            intent.putExtra(GameDifficulty.EXTRA_DIFFICULTY, difficulty);
        }

        try (ActivityScenario<Activity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                ViewGroup content = activity.findViewById(android.R.id.content);
                assertNotNull(content);
                assertEquals(1, content.getChildCount());
                assertEquals(expectedSimpleName, content.getChildAt(0).getClass().getSimpleName());
            });
        }
    }
}
