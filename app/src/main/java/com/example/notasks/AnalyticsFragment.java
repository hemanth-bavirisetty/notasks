package com.example.notasks;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsFragment extends Fragment {

    private PieChart taskStatusPieChart;
    private LineChart taskCompletionLineChart;
    private BarChart taskPriorityBarChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);

        taskStatusPieChart = view.findViewById(R.id.pie_chart_task_status);
        taskCompletionLineChart = view.findViewById(R.id.line_chart_task_completion);
        taskPriorityBarChart = view.findViewById(R.id.bar_chart_task_priority);

        setupTaskStatusPieChart();
        setupTaskCompletionLineChart();
        setupTaskPriorityBarChart();

        return view;
    }

    private void setupTaskStatusPieChart() {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(30f, "Yet to Start"));
        entries.add(new PieEntry(40f, "In Progress"));
        entries.add(new PieEntry(20f, "Completed"));
        entries.add(new PieEntry(10f, "On Hold"));

        PieDataSet dataSet = new PieDataSet(entries, "Task Status");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        taskStatusPieChart.setData(data);
        taskStatusPieChart.getDescription().setEnabled(false);
        taskStatusPieChart.animateY(1000);
    }

    private void setupTaskCompletionLineChart() {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0f, 4f));
        entries.add(new Entry(1f, 8f));
        entries.add(new Entry(2f, 6f));
        entries.add(new Entry(3f, 12f));
        entries.add(new Entry(4f, 18f));
        entries.add(new Entry(5f, 9f));
        entries.add(new Entry(6f, 16f));

        LineDataSet dataSet = new LineDataSet(entries, "Tasks Completed");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        LineData lineData = new LineData(dataSet);
        taskCompletionLineChart.setData(lineData);
        taskCompletionLineChart.getDescription().setEnabled(false);
        taskCompletionLineChart.animateX(1000);

        XAxis xAxis = taskCompletionLineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
    }

    private void setupTaskPriorityBarChart() {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, 10f));
        entries.add(new BarEntry(1f, 20f));
        entries.add(new BarEntry(2f, 15f));

        BarDataSet dataSet = new BarDataSet(entries, "Task Priority");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        taskPriorityBarChart.setData(barData);
        taskPriorityBarChart.getDescription().setEnabled(false);
        taskPriorityBarChart.animateY(1000);

        XAxis xAxis = taskPriorityBarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Low", "Medium", "High"}));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
    }
}

