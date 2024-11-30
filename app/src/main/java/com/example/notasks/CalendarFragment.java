package com.example.notasks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.time.LocalDate;
import java.time.ZoneId;

public class CalendarFragment extends Fragment implements OnDateSelectedListener {
    private MaterialCalendarView calendarView;
    private RecyclerView taskListForDate;
    private TaskAdapter taskAdapter;
    private final List<Task> tasks = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendar_view);
        taskListForDate = view.findViewById(R.id.taskListForDate);

        calendarView.setOnDateChangedListener(this);

        highlightDatesWithTasks();

        setupTaskList();

        return view;
    }


    private void setupTaskList() {
        taskAdapter = new TaskAdapter(tasks, new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Task task) {
                // Show task details
                Toast.makeText(getContext(), 
                    "Clicked: " + task.getTitle(), 
                    Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCompleteClick(Task task) {
                task.setStatus(Task.Status.COMPLETED);
                taskAdapter.notifyDataSetChanged();
                // Refresh calendar decorators
                calendarView.invalidateDecorators();
            }
        });

        taskListForDate.setLayoutManager(new LinearLayoutManager(getContext()));
        taskListForDate.setAdapter(taskAdapter);
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        updateTaskListForDate(date);
    }

    private void updateTaskListForDate(CalendarDay date) {
        // Clear current tasks
        tasks.clear();

        // Add tasks for selected date (sample data)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        //String selectedDateStr = sdf.format(date.getDate());
        
        // Convert String to Date
        Date selectedDate = null;
        try {
            selectedDate = sdf.parse(date.toString());
            //selectedDate = sdf.parse(selectedDateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        // Sample tasks for the selected date
        if (date.equals(CalendarDay.today())) {
            tasks.add(new Task(
                1,
                "Team Meeting",
                "Weekly team sync-up",
                selectedDate,
                Task.Priority.HIGH,
                Task.Status.YET_TO_START,
                "john.doe@example.com"
            ));
        }

        taskAdapter.notifyDataSetChanged();
    }

    private void highlightDatesWithTasks() {
        List<CalendarDay> dates = new ArrayList<>();
        dates.add(CalendarDay.today());
        dates.add(CalendarDay.from(2023, 5, 15));
        dates.add(CalendarDay.from(2023, 5, 20));

        calendarView.addDecorator(new EventDecorator(getContext(), dates));
    }

    private static class EventDecorator implements DayViewDecorator {
        private final int color;
        private final List<CalendarDay> dates;

        public EventDecorator(android.content.Context context, List<CalendarDay> dates) {
            this.color = context.getResources().getColor(com.google.android.material.R.color.material_dynamic_primary20);
            this.dates = dates;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new DotSpan(5, color));
        }
    }

    private String formatDate(LocalDate date) {
        Date convertedDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return formatter.format(convertedDate);
    }
}

