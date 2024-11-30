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

public class EditTaskDialogFragment extends DialogFragment {

    private static final String ARG_TASK = "task";

    private TextInputEditText titleInput;
    private TextInputEditText descriptionInput;
    private DatePicker deadlinePicker;
    private Spinner prioritySpinner;
    private Spinner statusSpinner;
    private DatabaseHelper databaseHelper;
    private GoogleCalendarHelper googleCalendarHelper;
    private Task task;
    private TaskEditedListener taskEditedListener;

    public static EditTaskDialogFragment newInstance(Task task) {
        EditTaskDialogFragment fragment = new EditTaskDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TASK, task);
        fragment.setArguments(args);
        return fragment;
    }

    public interface TaskEditedListener {
        void onTaskEdited(Task task);
    }

    public void setTaskEditedListener(TaskEditedListener listener) {
        this.taskEditedListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            task = (Task) getArguments().getSerializable(ARG_TASK);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_task, null);

        databaseHelper = new DatabaseHelper(requireContext());
        googleCalendarHelper = ((MainActivity) requireActivity()).getGoogleCalendarHelper();

        titleInput = view.findViewById(R.id.input_title);
        descriptionInput = view.findViewById(R.id.input_description);
        deadlinePicker = view.findViewById(R.id.date_picker_deadline);
        prioritySpinner = view.findViewById(R.id.spinner_priority);
        statusSpinner = view.findViewById(R.id.spinner_status);

        setupSpinners();
        populateFields();

        builder.setView(view)
                .setTitle("Edit Task")
                .setPositiveButton("Save", (dialog, id) -> updateTask())
                .setNegativeButton("Cancel", (dialog, id) -> EditTaskDialogFragment.this.getDialog().cancel());

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

    private void populateFields() {
        titleInput.setText(task.getTitle());
        descriptionInput.setText(task.getDescription());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(task.getDeadline());
        deadlinePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        prioritySpinner.setSelection(task.getPriority().ordinal());
        statusSpinner.setSelection(task.getStatus().ordinal());
    }

    private void updateTask() {
        task.setTitle(titleInput.getText().toString());
        task.setDescription(descriptionInput.getText().toString());
        task.setDeadline(getDateFromDatePicker(deadlinePicker));
        task.setPriority((Task.Priority) prioritySpinner.getSelectedItem());
        task.setStatus((Task.Status) statusSpinner.getSelectedItem());

        databaseHelper.updateTask(task);
        if (googleCalendarHelper != null) {
            googleCalendarHelper.updateTaskInCalendar(task);
        }

        if (taskEditedListener != null) {
            taskEditedListener.onTaskEdited(task);
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
