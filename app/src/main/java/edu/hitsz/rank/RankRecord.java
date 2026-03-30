package edu.hitsz.rank;

import edu.hitsz.GameDifficulty;

public class RankRecord {
    private final long id;
    private final int score;
    private final String difficulty;
    private final String playedAt;

    public RankRecord(int score, String difficulty, String playedAt) {
        this(0L, score, difficulty, playedAt);
    }

    public RankRecord(long id, int score, String difficulty, String playedAt) {
        this.id = id;
        this.score = score;
        this.difficulty = GameDifficulty.normalize(difficulty);
        this.playedAt = playedAt;
    }

    public long getId() {
        return id;
    }

    public int getScore() {
        return score;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getPlayedAt() {
        return playedAt;
    }
}
