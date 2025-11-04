package edu.uga.cs.statecapitalsquiz;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Main activity
 */
public class MainActivity extends AppCompatActivity
        implements LoadStatesTask.Listener,
        SplashFragment.QuizHost,
        CreateQuizTask.Listener {

    private StatesDbHelper dbHelper;

    /**
     * Creates activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new StatesDbHelper(this);

        new LoadStatesTask(this, dbHelper, this).execute();
    }

    /**
     * Callback when states load
     */
    @Override
    public void onStatesLoaded() {
        SharedPreferences prefs = getSharedPreferences("quiz_state", MODE_PRIVATE);
        long savedQuizId = prefs.getLong("current_quiz_id", -1L);
        int savedPos = prefs.getInt("current_position", -1);

        if (savedQuizId != -1L && savedPos != -1) {
            if (isQuizUnfinished(savedQuizId)) {
                QuizFragment frag = QuizFragment.newInstance(savedQuizId, savedPos);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, frag)
                        .commit();
                return;
            } else {
                clearSavedQuizState();
            }
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new SplashFragment())
                .commit();
    }

    /**
     * Creates new quiz
     */
    @Override
    public void createNewQuiz() {
        new CreateQuizTask(this, dbHelper).execute();
    }

    /**
     * Callback after quiz creation
     */
    @Override
    public void onQuizCreated(long quizId) {
        getSharedPreferences("quiz_state", MODE_PRIVATE)
                .edit()
                .putLong("current_quiz_id", quizId)
                .putInt("current_position", 0)
                .apply();

        QuizFragment frag = QuizFragment.newInstance(quizId, 0);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, frag)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Shows quiz history
     */
    @Override
    public void showHistory() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new HistoryFragment())
                .addToBackStack(null)
                .commit();
    }

    /**
     * Calls db helper
     */
    public StatesDbHelper getDbHelper() {
        return dbHelper;
    }

    /**
     * Checks if quiz is unfinished
     */
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

    /**
     * Clears saved quiz state
     */
    private void clearSavedQuizState() {
        getSharedPreferences("quiz_state", MODE_PRIVATE)
                .edit()
                .remove("current_quiz_id")
                .remove("current_position")
                .apply();
    }
}