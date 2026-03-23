package edu.hitsz;

public final class GameDifficulty {
    public static final String EXTRA_DIFFICULTY = "edu.hitsz.extra.DIFFICULTY";
    public static final String EASY = "easy";
    public static final String NORMAL = "normal";
    public static final String HARD = "hard";

    private GameDifficulty() {
    }

    public static String normalize(String difficulty) {
        if (EASY.equals(difficulty)) {
            return EASY;
        }
        if (NORMAL.equals(difficulty)) {
            return NORMAL;
        }
        if (HARD.equals(difficulty)) {
            return HARD;
        }
        return EASY;
    }
}
