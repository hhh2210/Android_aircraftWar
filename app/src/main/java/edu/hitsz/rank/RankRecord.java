package edu.hitsz.rank;

import edu.hitsz.GameDifficulty;

public class RankRecord {
    private final long id;
    private final int score;
    private final String difficulty;
    private final String playedAt;
    private final String username;

    public RankRecord(int score, String difficulty, String playedAt) {
        this(0L, score, difficulty, playedAt, "");
    }

    public RankRecord(int score, String difficulty, String playedAt, String username) {
        this(0L, score, difficulty, playedAt, username);
    }

    public RankRecord(long id, int score, String difficulty, String playedAt, String username) {
        this.id = id;
        this.score = score;
        this.difficulty = GameDifficulty.normalize(difficulty);
        this.playedAt = playedAt;
        this.username = username != null ? username : "";
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

    public String getUsername() {
        return username;
    }
}
