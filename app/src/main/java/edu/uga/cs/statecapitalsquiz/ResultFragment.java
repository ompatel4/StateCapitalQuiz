package edu.uga.cs.statecapitalsquiz;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Fragment displays quiz results
 */
public class ResultFragment extends Fragment {

    private static final String ARG_QUIZ_ID = "quiz_id";
    private long quizId;
    private StatesDbHelper dbHelper;

    /**
     * Creates new instance of the fragment
     */
    public static ResultFragment newInstance(long quizId) {
        ResultFragment f = new ResultFragment();
        Bundle b = new Bundle();
        b.putLong(ARG_QUIZ_ID, quizId);
        f.setArguments(b);
        return f;
    }

    /**
     * Creates view for result fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_result, container, false);
    }

    /**
     * Sets up view
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = ((MainActivity) requireActivity()).getDbHelper();

        if (getArguments() != null) {
            quizId = getArguments().getLong(ARG_QUIZ_ID);
        }

        TextView resultText = view.findViewById(R.id.text_result);
        Button homeBtn = view.findViewById(R.id.button_home);
        Button historyBtn = view.findViewById(R.id.button_history);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT score, total_questions FROM quizzes WHERE id = ?",
                new String[]{ String.valueOf(quizId) });
        if (c.moveToFirst()) {
            int score = c.getInt(0);
            int total = c.getInt(1);
            resultText.setText("You scored " + score + " / " + total);
        }
        c.close();

        db.execSQL("UPDATE quizzes SET finished_at = datetime('now') WHERE id = " + quizId);

        homeBtn.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new SplashFragment())
                    .commit();
        });

        historyBtn.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HistoryFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }
}