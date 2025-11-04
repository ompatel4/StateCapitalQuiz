package edu.uga.cs.statecapitalsquiz;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment displays quiz history
 */
public class HistoryFragment extends Fragment implements LoadHistoryTask.Listener {

    private StatesDbHelper dbHelper;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> items = new ArrayList<>();
    private TextView emptyView;

    public HistoryFragment() {
    }

    /**
     * Creates view for the history fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    /**
     * Sets up view
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = ((MainActivity) requireActivity()).getDbHelper();

        ListView listView = view.findViewById(R.id.history_list);
        emptyView = view.findViewById(R.id.history_empty);

        adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                items
        );
        listView.setAdapter(adapter);
        listView.setEmptyView(emptyView);

        new LoadHistoryTask(dbHelper, this).execute();
    }

    /**
     * Callback when history loads
     */
    @Override
    public void onHistoryLoaded(List<String> newItems) {
        items.clear();
        items.addAll(newItems);
        adapter.notifyDataSetChanged();

        if (items.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }
}