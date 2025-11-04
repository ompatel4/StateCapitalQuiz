package edu.uga.cs.statecapitalsquiz;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Fragment displays quiz questions
 */
public class QuizFragment extends Fragment {

    private static final String ARG_QUIZ_ID = "quiz_id";
    private static final String ARG_POSITION = "position";

    private long quizId;
    private int position;

    private StatesDbHelper dbHelper;

    private TextView questionText;
    private RadioGroup choicesGroup;
    private RadioButton choice1, choice2, choice3;
    private TextView counterText;

    private long quizQuestionId;
    private String correctAnswer;
    private float xDown = 0f;
    private static final int SWIPE_THRESHOLD = 150;


    /**
     * Creates a new instance of fragment
     */
    public static QuizFragment newInstance(long quizId, int position) {
        QuizFragment f = new QuizFragment();
        Bundle b = new Bundle();
        b.putLong(ARG_QUIZ_ID, quizId);
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    /**
     * Attaches fragment to context
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            dbHelper = ((MainActivity) context).getDbHelper();
        }
    }

    /**
     * Creates view for quiz fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_quiz, container, false);

        questionText = root.findViewById(R.id.text_question);
        choicesGroup = root.findViewById(R.id.radio_group);
        choice1 = root.findViewById(R.id.radio_choice1);
        choice2 = root.findViewById(R.id.radio_choice2);
        choice3 = root.findViewById(R.id.radio_choice3);
        counterText = root.findViewById(R.id.text_counter);

        if (getArguments() != null) {
            quizId = getArguments().getLong(ARG_QUIZ_ID);
            position = getArguments().getInt(ARG_POSITION);
        }

        loadQuestion();

        root.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    xDown = event.getX();
                    return true;
                case MotionEvent.ACTION_UP:
                    float xUp = event.getX();
                    float deltaX = xUp - xDown;
                    if (deltaX < -SWIPE_THRESHOLD) {
                        onSwipeLeft();
                        return true;
                    }
                    return false;
            }
            return false;
        });

        return root;
    }

    /**
     * Loads quiz question data
     */
    private void loadQuestion() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT qq.id, s.state_name, s.capital, s.city2, s.city3 " +
                        "FROM quiz_questions qq " +
                        "JOIN states s ON qq.state_id = s.id " +
                        "WHERE qq.quiz_id = ? AND qq.position = ?",
                new String[]{ String.valueOf(quizId), String.valueOf(position) }
        );
        if (c.moveToFirst()) {
            quizQuestionId = c.getLong(0);
            String stateName = c.getString(1);
            String capital = c.getString(2);
            String city2 = c.getString(3);
            String city3 = c.getString(4);
            correctAnswer = capital;

            questionText.setText("What is the capital of " + stateName + "?");
            counterText.setText("Question " + (position + 1) + " of 6");

            ArrayList<String> choices = new ArrayList<>();
            choices.add(capital);
            choices.add(city2);
            choices.add(city3);
            Collections.shuffle(choices, new Random());

            choice1.setText(choices.get(0));
            choice2.setText(choices.get(1));
            choice3.setText(choices.get(2));

            choicesGroup.clearCheck();
        }
        c.close();
    }

    /**
     * Handles left swipe
     */
    private void onSwipeLeft() {
        int checkedId = choicesGroup.getCheckedRadioButtonId();
        if (checkedId == -1) {
            return;
        }

        RadioButton selected = requireView().findViewById(checkedId);
        String userAnswer = selected.getText().toString();
        boolean isCorrect = userAnswer.equals(correctAnswer);

        saveAnswer(userAnswer, isCorrect);

        if (position < 5) {
            QuizFragment next = QuizFragment.newInstance(quizId, position + 1);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, next)
                    .commit();
        } else {
            ResultFragment res = ResultFragment.newInstance(quizId);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, res)
                    .commit();
        }
    }

    /**
     * Saves the users answer to db
     */
    private void saveAnswer(String userAnswer, boolean isCorrect) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        android.content.ContentValues v = new android.content.ContentValues();
        v.put("user_answer", userAnswer);
        v.put("is_correct", isCorrect ? 1 : 0);
        db.update("quiz_questions", v, "id = ?", new String[]{ String.valueOf(quizQuestionId) });

        String incScore = isCorrect ? ", score = score + 1" : "";
        db.execSQL("UPDATE quizzes SET answered_questions = answered_questions + 1"
                + incScore + " WHERE id = " + quizId);
    }

    /**
     * Pauses fragment and saves state
     */
    @Override
    public void onPause() {
        super.onPause();
        requireContext().getSharedPreferences("quiz_state", Context.MODE_PRIVATE)
                .edit()
                .putLong("current_quiz_id", quizId)
                .putInt("current_position", position)
                .apply();
    }

}