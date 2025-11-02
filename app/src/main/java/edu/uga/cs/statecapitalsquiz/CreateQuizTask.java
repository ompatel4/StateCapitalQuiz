package edu.uga.cs.statecapitalsquiz;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CreateQuizTask extends AsyncTask<Void, Void, Long> {

    public interface Listener {
        void onQuizCreated(long quizId);
    }

    private final Context context;
    private final StatesDbHelper dbHelper;
    private final Listener listener;

    public CreateQuizTask(Context context, StatesDbHelper dbHelper) {
        this.context = context.getApplicationContext();
        this.dbHelper = dbHelper;
        this.listener = (context instanceof Listener) ? (Listener) context : null;
    }

    @Override
    protected Long doInBackground(Void... voids) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // 1. select 6 random states
        Cursor c = db.rawQuery(
                "SELECT id FROM states ORDER BY RANDOM() LIMIT 6",
                null
        );
        ArrayList<Long> stateIds = new ArrayList<>();
        while (c.moveToNext()) {
            stateIds.add(c.getLong(0));
        }
        c.close();

        // 2. create quiz
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                .format(new Date());

        ContentValues qv = new ContentValues();
        qv.put("created_at", now);
        qv.put("total_questions", 6);
        qv.put("answered_questions", 0);
        qv.put("score", 0);
        long quizId = db.insert("quizzes", null, qv);

        // 3. create quiz_questions
        for (int i = 0; i < stateIds.size(); i++) {
            ContentValues v = new ContentValues();
            v.put("quiz_id", quizId);
            v.put("state_id", stateIds.get(i));
            v.put("position", i);
            db.insert("quiz_questions", null, v);
        }

        return quizId;
    }

    @Override
    protected void onPostExecute(Long quizId) {
        if (listener != null) {
            listener.onQuizCreated(quizId);
        }
    }
}
