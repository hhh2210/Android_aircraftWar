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
        assertEquals(GameDifficulty.EASY, GameDifficulty.normalize("impossible"));
    }

    @Test
    public void toDisplayName_returnsExpectedLabels() {
        assertEquals("Easy", GameDifficulty.toDisplayName(GameDifficulty.EASY));
        assertEquals("Normal", GameDifficulty.toDisplayName(GameDifficulty.NORMAL));
        assertEquals("Hard", GameDifficulty.toDisplayName(GameDifficulty.HARD));
    }
}
