package com.example.notasks;

import android.content.Context;
import androidx.core.content.ContextCompat;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.Collection;

public class EventDecorator implements DayViewDecorator {

    private final int color;
    private final Collection<CalendarDay> dates;

    public EventDecorator(Context context, Collection<CalendarDay> dates) {
        this.color = ContextCompat.getColor(context, R.color.md_theme_inversePrimary_mediumContrast);
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

