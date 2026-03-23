package edu.hitsz;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
public class MainActivityScreenTest {

    @Test
    public void displaysDifficultySelectionScreen() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.text_title)).check(matches(allOf(
                    isDisplayed(),
                    withText(R.string.main_title)
            )));
            onView(withId(R.id.text_subtitle)).check(matches(allOf(
                    isDisplayed(),
                    withText(R.string.main_subtitle)
            )));
            onView(withId(R.id.button_easy)).check(matches(allOf(
                    isDisplayed(),
                    withText(R.string.difficulty_easy)
            )));
            onView(withId(R.id.button_normal)).check(matches(allOf(
                    isDisplayed(),
                    withText(R.string.difficulty_normal)
            )));
            onView(withId(R.id.button_hard)).check(matches(allOf(
                    isDisplayed(),
                    withText(R.string.difficulty_hard)
            )));
        }
    }
}
