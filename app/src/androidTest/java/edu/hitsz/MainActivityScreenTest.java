package edu.hitsz;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class MainActivityScreenTest {

    @Test
    public void displaysDifficultySelectionScreen() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                assertDisplayedText(activity, R.id.text_title, R.string.main_title);
                assertDisplayedText(activity, R.id.text_subtitle, R.string.main_subtitle);
                assertDisplayedText(activity, R.id.button_easy, R.string.difficulty_easy);
                assertDisplayedText(activity, R.id.button_normal, R.string.difficulty_normal);
                assertDisplayedText(activity, R.id.button_hard, R.string.difficulty_hard);
            });
        }
    }

    private void assertDisplayedText(MainActivity activity, int viewId, int stringId) {
        android.widget.TextView view = activity.findViewById(viewId);
        assertTrue(view.isShown());
        assertEquals(activity.getString(stringId), view.getText().toString());
    }
}
