package edu.uga.cs.statecapitalsquiz;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StatesDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "state_capitals.db";
    public static final int DB_VERSION = 2;
    private final Context context;

    public StatesDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE states (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "state_name TEXT NOT NULL, " +
                "capital TEXT NOT NULL, " +
                "city2 TEXT NOT NULL, " +
                "city3 TEXT NOT NULL" +
                ");");

        db.execSQL("CREATE TABLE quizzes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "created_at TEXT NOT NULL, " +
                "finished_at TEXT, " +
                "total_questions INTEGER NOT NULL, " +
                "answered_questions INTEGER NOT NULL DEFAULT 0, " +
                "score INTEGER NOT NULL DEFAULT 0" +
                ");");

        db.execSQL("CREATE TABLE quiz_questions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "quiz_id INTEGER NOT NULL, " +
                "state_id INTEGER NOT NULL, " +
                "user_answer TEXT, " +
                "is_correct INTEGER, " +
                "position INTEGER NOT NULL, " +
                "FOREIGN KEY (quiz_id) REFERENCES quizzes(id), " +
                "FOREIGN KEY (state_id) REFERENCES states(id)" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS quiz_questions");
        db.execSQL("DROP TABLE IF EXISTS quizzes");
        db.execSQL("DROP TABLE IF EXISTS states");
        onCreate(db);
    }
}
