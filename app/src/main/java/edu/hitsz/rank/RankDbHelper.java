package edu.hitsz.rank;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class RankDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "rank.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_NAME = "rank_records";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_SCORE = "score";
    private static final String COLUMN_DIFFICULTY = "difficulty";
    private static final String COLUMN_PLAYED_AT = "played_at";

    public RankDbHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_SCORE + " INTEGER NOT NULL, "
                + COLUMN_DIFFICULTY + " TEXT NOT NULL, "
                + COLUMN_PLAYED_AT + " TEXT NOT NULL"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public long insert(RankRecord record) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SCORE, record.getScore());
        values.put(COLUMN_DIFFICULTY, record.getDifficulty());
        values.put(COLUMN_PLAYED_AT, record.getPlayedAt());
        return db.insert(TABLE_NAME, null, values);
    }

    public List<RankRecord> queryAllOrderByScoreDesc() {
        List<RankRecord> records = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(
                TABLE_NAME,
                new String[]{COLUMN_ID, COLUMN_SCORE, COLUMN_DIFFICULTY, COLUMN_PLAYED_AT},
                null,
                null,
                null,
                null,
                COLUMN_SCORE + " DESC, " + COLUMN_PLAYED_AT + " DESC"
        )) {
            while (cursor.moveToNext()) {
                records.add(new RankRecord(
                        cursor.getLong(0),
                        cursor.getInt(1),
                        cursor.getString(2),
                        cursor.getString(3)
                ));
            }
        }
        return records;
    }

    public int deleteById(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }
}
