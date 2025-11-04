package edu.uga.cs.statecapitalsquiz;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

/**
 * AsyncTask loads quiz history
 */
public class LoadHistoryTask extends AsyncTask<Void, Void, List<String>> {

    /**
     * Listener interface for history callback
     */
    public interface Listener {
        void onHistoryLoaded(List<String> items);
    }

    private final StatesDbHelper dbHelper;
    private final Listener listener;

    public LoadHistoryTask(StatesDbHelper dbHelper, Listener listener) {
        this.dbHelper = dbHelper;
        this.listener = listener;
    }

    /**
     * Loads history in background
     */
    @Override
    protected List<String> doInBackground(Void... voids) {
        List<String> results = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT created_at, finished_at, score, total_questions " +
                        "FROM quizzes " +
                        "WHERE finished_at IS NOT NULL " +
                        "ORDER BY datetime(finished_at) DESC",
                null
        );

        while (c.moveToNext()) {
            String finished = c.getString(1);
            int score = c.getInt(2);
            int total = c.getInt(3);

            String line = (finished != null ? finished : "Unknown time")
                    + "  —  Score: " + score + "/" + total;
            results.add(line);
        }
        c.close();

        return results;
    }

    /**
     * Callback after history loads
     */
    @Override
    protected void onPostExecute(List<String> items) {
        if (listener != null) {
            listener.onHistoryLoaded(items);
        }
    }
}