package edu.uga.cs.statecapitalsquiz;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity
        implements LoadStatesTask.Listener,
        SplashFragment.QuizHost,
        CreateQuizTask.Listener {

    private StatesDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);   // must have fragment_container

        dbHelper = new StatesDbHelper(this);

        // load CSV → DB in background
        new LoadStatesTask(this, dbHelper, this).execute();
    }

    @Override
    public void onStatesLoaded() {
        // after DB is ready, see if there was a quiz in progress
        SharedPreferences prefs = getSharedPreferences("quiz_state", MODE_PRIVATE);
        long savedQuizId = prefs.getLong("current_quiz_id", -1L);
        int savedPos = prefs.getInt("current_position", -1);

        if (savedQuizId != -1L && savedPos != -1) {
            // check if that quiz is actually unfinished in DB
            if (isQuizUnfinished(savedQuizId)) {
                // resume quiz
                QuizFragment frag = QuizFragment.newInstance(savedQuizId, savedPos);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, frag)
                        .commit();
                return;
            } else {
                // finished already → clear it
                clearSavedQuizState();
            }
        }

        // otherwise show home
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new SplashFragment())
                .commit();
    }

    @Override
    public void createNewQuiz() {
        // user clicked "Start New Quiz" on splash
        new CreateQuizTask(this, dbHelper).execute();
    }

    @Override
    public void onQuizCreated(long quizId) {
        // remember this quiz as current (pos = 0)
        getSharedPreferences("quiz_state", MODE_PRIVATE)
                .edit()
                .putLong("current_quiz_id", quizId)
                .putInt("current_position", 0)
                .apply();

        // open first question
        QuizFragment frag = QuizFragment.newInstance(quizId, 0);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, frag)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void showHistory() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new HistoryFragment())
                .addToBackStack(null)
                .commit();
    }

    // --- helpers ---

    public StatesDbHelper getDbHelper() {
        return dbHelper;
    }

    private boolean isQuizUnfinished(long quizId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT answered_questions, total_questions " +
                        "FROM quizzes WHERE id = ?",
                new String[]{ String.valueOf(quizId) }
        );
        boolean unfinished = false;
        if (c.moveToFirst()) {
            int answered = c.getInt(0);
            int total = c.getInt(1);
            unfinished = answered < total;
        }
        c.close();
        return unfinished;
    }

    private void clearSavedQuizState() {
        getSharedPreferences("quiz_state", MODE_PRIVATE)
                .edit()
                .remove("current_quiz_id")
                .remove("current_position")
                .apply();
    }
}
