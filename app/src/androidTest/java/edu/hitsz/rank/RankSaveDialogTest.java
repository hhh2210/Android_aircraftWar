package edu.hitsz.rank;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import edu.hitsz.GameDifficulty;
import edu.hitsz.MainActivity;
import edu.hitsz.R;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class RankSaveDialogTest {

    private Context context;
    private RankDbHelper dbHelper;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase("rank.db");
        dbHelper = new RankDbHelper(context);
    }

    @After
    public void tearDown() {
        dbHelper.close();
    }

    @Test
    public void confirmWithBlankUsernameSavesDefaultUserScoreAndDifficulty() {
        AtomicBoolean callbackCalled = new AtomicBoolean(false);
        AtomicReference<AlertDialog> dialogRef = new AtomicReference<>();

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> dialogRef.set(RankSaveDialog.show(
                            activity,
                            dbHelper,
                            321,
                            GameDifficulty.NORMAL,
                            () -> callbackCalled.set(true)
                    ))
            );

            scenario.onActivity(activity ->
                    dialogRef.get().getButton(AlertDialog.BUTTON_POSITIVE).performClick());
        }

        List<RankRecord> records = dbHelper.queryByDifficulty(GameDifficulty.NORMAL);
        assertEquals(1, records.size());
        assertEquals(321, records.get(0).getScore());
        assertEquals(GameDifficulty.NORMAL, records.get(0).getDifficulty());
        assertEquals(context.getString(R.string.dialog_username_default), records.get(0).getUsername());
        assertTrue(callbackCalled.get());
    }
}
