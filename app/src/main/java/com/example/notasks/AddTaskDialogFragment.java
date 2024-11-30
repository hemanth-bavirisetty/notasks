package com.example.notasks;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Calendar;
import java.util.Date;

public class AddTaskDialogFragment extends DialogFragment {

    private static final String ARG_USER_ID = "user_id";

    private TextInputEditText titleInput;
    private TextInputEditText descriptionInput;
    private DatePicker deadlinePicker;
    private Spinner prioritySpinner;
    private Spinner statusSpinner;
    private DatabaseHelper databaseHelper;
    private GoogleCalendarHelper googleCalendarHelper;
    private TaskAddedListener taskAddedListener;
    private long userId;

    public static AddTaskDialogFragment newInstance(long userId) {
        AddTaskDialogFragment fragment = new AddTaskDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_USER_ID, userId);
        return fragment;
    }

    public interface TaskAddedListener {
        void onTaskAdded(Task task);
    }

    public void setTaskAddedListener(TaskAddedListener listener) {
        this.taskAddedListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getLong(ARG_USER_ID);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_task, null);

        databaseHelper = new DatabaseHelper(requireContext());
        googleCalendarHelper = ((MainActivity) requireActivity()).getGoogleCalendarHelper();

        titleInput = view.findViewById(R.id.input_title);
        descriptionInput = view.findViewById(R.id.input_description);
        deadlinePicker = view.findViewById(R.id.date_picker_deadline);
        prioritySpinner = view.findViewById(R.id.spinner_priority);
        statusSpinner = view.findViewById(R.id.spinner_status);

        setupSpinners();

        builder.setView(view)
                .setTitle("Add New Task")
                .setPositiveButton("Add", (dialog, id) -> addTask())
                .setNegativeButton("Cancel", (dialog, id) -> AddTaskDialogFragment.this.getDialog().cancel());

        return builder.create();
    }

    private void setupSpinners() {
        ArrayAdapter<Task.Priority> priorityAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, Task.Priority.values());
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(priorityAdapter);

        ArrayAdapter<Task.Status> statusAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, Task.Status.values());
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);
    }

    private void addTask() {
        String title = titleInput.getText().toString();
        String description = descriptionInput.getText().toString();
        Date deadline = getDateFromDatePicker(deadlinePicker);
        Task.Priority priority = (Task.Priority) prioritySpinner.getSelectedItem();
        Task.Status status = (Task.Status) statusSpinner.getSelectedItem();

        Task newTask = new Task(
            0,
            title,
            description,
            deadline,
            priority,
            status,
            String.valueOf(userId)
        );

        long taskId = databaseHelper.addTask(newTask);
        if (taskId != -1) {
            newTask.setId((int)taskId);
            if (googleCalendarHelper != null) {
                googleCalendarHelper.addTaskToCalendar(newTask);
            }
            if (taskAddedListener != null) {
                taskAddedListener.onTaskAdded(newTask);
            }
        }
    }

    private Date getDateFromDatePicker(DatePicker datePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        return calendar.getTime();
    }
}
