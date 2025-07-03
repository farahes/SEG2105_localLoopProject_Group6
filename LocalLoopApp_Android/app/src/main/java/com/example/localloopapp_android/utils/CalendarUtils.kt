package com.example.localloopapp_android.utils

import android.view.View
import android.widget.TextView
import com.example.localloopapp_android.R
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.CalendarView
import java.time.LocalDate

class DayViewContainer(view: View) : ViewContainer(view) {
    val textView: TextView = view.findViewById(R.id.calendarDayText)
    val dotView: View = view.findViewById(R.id.calendarDot)
    lateinit var day: CalendarDay
}

fun setupCalendar(
    calendarView: CalendarView,
    eventDates: Set<LocalDate>
) {
    // Define how each day should look
    calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
        override fun create(view: View): DayViewContainer {
            return DayViewContainer(view)
        }

        override fun bind(container: DayViewContainer, data: CalendarDay) {
            container.day = data
            container.textView.text = data.date.dayOfMonth.toString()
            val today = LocalDate.now()
            val eventDate = data.date

            if (eventDates.contains(eventDate)) {
                container.dotView.visibility = View.VISIBLE

                if (!eventDate.isAfter(today)) {
                    container.dotView.setBackgroundResource(R.drawable.blue_circle) // 🔵 past
                } else {
                    container.dotView.setBackgroundResource(R.drawable.red_circle) // 🔴 upcoming
                }
            } else {
                container.dotView.visibility = View.GONE
            }

        }
    }
}
