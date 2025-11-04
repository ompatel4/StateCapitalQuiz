package edu.uga.cs.statecapitalsquiz;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LoadStatesTask extends AsyncTask<Void, Void, Void> {

    public interface Listener {
        void onStatesLoaded();
    }

    private final Context context;
    private final StatesDbHelper dbHelper;
    private final Listener listener;

    public LoadStatesTask(Context context, StatesDbHelper dbHelper, Listener listener) {
        this.context = context.getApplicationContext();
        this.dbHelper = dbHelper;
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor c = db.rawQuery("SELECT COUNT(*) FROM states", null);
        int count = 0;
        if (c.moveToFirst()) {
            count = c.getInt(0);
        }
        c.close();
        if (count > 0) {
            return null;
        }

        try {
            InputStream is = context.getAssets().open("state_capitals.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            boolean first = true;

            db.beginTransaction();
            while ((line = reader.readLine()) != null) {
                if (first) {
                    first = false;
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length < 4) {
                    continue;
                }
                String state = parts[0].trim();
                String capital = parts[1].trim();
                String city2 = parts[2].trim();
                String city3 = parts[3].trim();

                ContentValues values = new ContentValues();
                values.put("state_name", state);
                values.put("capital", capital);
                values.put("city2", city2);
                values.put("city3", city3);
                db.insert("states", null, values);
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (listener != null) {
            listener.onStatesLoaded();
        }
    }
}
