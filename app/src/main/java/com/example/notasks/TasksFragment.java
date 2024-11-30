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

public class TasksFragment extends Fragment implements 
    AddTaskDialogFragment.TaskAddedListener,
    EditTaskDialogFragment.TaskEditedListener {

    private TaskAdapter adapter;
    private List<Task> taskList;
    private DatabaseHelper databaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(requireContext());

        // Initialize RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize task list and fetch tasks
        taskList = new ArrayList<>();
        loadTasks();

        // Initialize adapter with click listeners
        adapter = new TaskAdapter(taskList, new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Task task) {
                showEditTaskDialog(task);
            }

            @Override
            public void onCompleteClick(Task task) {
                task.setStatus(Task.Status.COMPLETED);
                updateTask(task);
            }
        });
        recyclerView.setAdapter(adapter);

        // Setup FAB
        FloatingActionButton fabAddTask = view.findViewById(R.id.fab_add_task);
        fabAddTask.setOnClickListener(v -> showAddTaskDialog());

        return view;
    }

    private void loadTasks() {
        // Assuming user ID 1 for now - you should get the actual logged-in user ID
        taskList.clear();
        taskList.addAll(databaseHelper.getUserTasks(1));
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void showAddTaskDialog() {
        AddTaskDialogFragment dialog = AddTaskDialogFragment.newInstance(1); // Using user ID 1
        dialog.setTaskAddedListener(this);
        dialog.show(getParentFragmentManager(), "AddTaskDialog");
    }

    private void showEditTaskDialog(Task task) {
        EditTaskDialogFragment dialog = EditTaskDialogFragment.newInstance(task);
        dialog.setTaskEditedListener(this);
        dialog.show(getParentFragmentManager(), "EditTaskDialog");
    }

    private void updateTask(Task task) {
        int result = databaseHelper.updateTask(task);
        if (result > 0) {
            loadTasks(); // Reload all tasks to reflect changes
        }
    }

    private void deleteTask(Task task) {
        databaseHelper.deleteTask(task.getId());
        loadTasks(); // Reload all tasks to reflect changes
    }

    @Override
    public void onTaskAdded(Task task) {
        loadTasks(); // Reload all tasks to include the new task
    }

    @Override
    public void onTaskEdited(Task task) {
        loadTasks(); // Reload all tasks to reflect changes
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}

