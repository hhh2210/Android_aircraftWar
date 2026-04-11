package edu.hitsz.rank;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class RankDbHelperTest {

    private RankDbHelper dbHelper;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase("rank.db");
        dbHelper = new RankDbHelper(context);
    }

    @After
    public void tearDown() {
        dbHelper.close();
    }

    @Test
    public void queryByDifficulty_returnsOnlyMatchingRecords() {
        dbHelper.insert(new RankRecord(100, "easy", "2026-01-01 10:00:00"));
        dbHelper.insert(new RankRecord(200, "normal", "2026-01-01 11:00:00"));
        dbHelper.insert(new RankRecord(150, "easy", "2026-01-01 12:00:00"));
        dbHelper.insert(new RankRecord(300, "hard", "2026-01-01 13:00:00"));

        List<RankRecord> easyRecords = dbHelper.queryByDifficulty("easy");
        assertEquals(2, easyRecords.size());
        for (RankRecord r : easyRecords) {
            assertEquals("easy", r.getDifficulty());
        }

        List<RankRecord> normalRecords = dbHelper.queryByDifficulty("normal");
        assertEquals(1, normalRecords.size());
        assertEquals("normal", normalRecords.get(0).getDifficulty());

        List<RankRecord> hardRecords = dbHelper.queryByDifficulty("hard");
        assertEquals(1, hardRecords.size());
    }

    @Test
    public void queryByDifficulty_returnsEmptyForNoMatches() {
        dbHelper.insert(new RankRecord(100, "easy", "2026-01-01 10:00:00"));

        List<RankRecord> result = dbHelper.queryByDifficulty("hard");
        assertTrue(result.isEmpty());
    }

    @Test
    public void queryByDifficulty_nullFallsBackToAll() {
        dbHelper.insert(new RankRecord(100, "easy", "2026-01-01 10:00:00"));
        dbHelper.insert(new RankRecord(200, "normal", "2026-01-01 11:00:00"));

        List<RankRecord> result = dbHelper.queryByDifficulty(null);
        assertEquals(2, result.size());
    }

    @Test
    public void queryByDifficulty_orderedByScoreDesc() {
        dbHelper.insert(new RankRecord(50, "easy", "2026-01-01 10:00:00"));
        dbHelper.insert(new RankRecord(300, "easy", "2026-01-01 11:00:00"));
        dbHelper.insert(new RankRecord(150, "easy", "2026-01-01 12:00:00"));

        List<RankRecord> result = dbHelper.queryByDifficulty("easy");
        assertEquals(3, result.size());
        assertTrue(result.get(0).getScore() >= result.get(1).getScore());
        assertTrue(result.get(1).getScore() >= result.get(2).getScore());
    }

    @Test
    public void queryAll_stillWorksAfterAddingByDifficulty() {
        dbHelper.insert(new RankRecord(100, "easy", "2026-01-01 10:00:00"));
        dbHelper.insert(new RankRecord(200, "hard", "2026-01-01 11:00:00"));

        List<RankRecord> all = dbHelper.queryAllOrderByScoreDesc();
        assertEquals(2, all.size());
        assertEquals(200, all.get(0).getScore());
    }
}
