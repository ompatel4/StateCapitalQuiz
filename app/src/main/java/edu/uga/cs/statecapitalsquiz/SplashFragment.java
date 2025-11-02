package edu.uga.cs.statecapitalsquiz;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class SplashFragment extends Fragment {

    public SplashFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_splash, container, false);

        Button startQuizBtn = root.findViewById(R.id.button_start_quiz);
        Button viewHistoryBtn = root.findViewById(R.id.button_view_history);

        startQuizBtn.setOnClickListener(v -> {
            // ask activity to create a quiz
            if (getActivity() instanceof QuizHost) {
                ((QuizHost) getActivity()).createNewQuiz();
            }
        });

        viewHistoryBtn.setOnClickListener(v -> {
            if (getActivity() instanceof QuizHost) {
                ((QuizHost) getActivity()).showHistory();
            }
        });

        return root;
    }

    public interface QuizHost {
        void createNewQuiz();
        void showHistory();
    }
}
