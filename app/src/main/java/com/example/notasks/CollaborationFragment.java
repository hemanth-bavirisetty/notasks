package com.example.notasks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class CollaborationFragment extends Fragment {

    private RecyclerView sharedTasksRecyclerView;
    private SharedTaskAdapter sharedTaskAdapter;
    private List<SharedTask> sharedTasks;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collaboration, container, false);

        sharedTasksRecyclerView = view.findViewById(R.id.recycler_view_shared_tasks);
        sharedTasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        sharedTasks = new ArrayList<>();
        // TODO: Fetch shared tasks from database
        sharedTaskAdapter = new SharedTaskAdapter(sharedTasks);
        sharedTasksRecyclerView.setAdapter(sharedTaskAdapter);

        FloatingActionButton fabShareTask = view.findViewById(R.id.fab_share_task);
        fabShareTask.setOnClickListener(v -> showShareTaskDialog());

        return view;
    }

    private void showShareTaskDialog() {
        // TODO: Implement share task dialog
    }
}

