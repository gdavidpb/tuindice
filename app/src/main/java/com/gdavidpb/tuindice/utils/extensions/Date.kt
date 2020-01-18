package com.gdavidpb.tuindice.utils.extensions

import com.gdavidpb.tuindice.utils.DEFAULT_LOCALE
import java.util.*

fun Date.isToday(): Boolean {
    return checkInRange({
        precision(Calendar.DATE)
    }, {
        precision(Calendar.DATE)

        add(Calendar.DATE, 1)
        add(Calendar.SECOND, -1)
    })
}

fun Date.isTomorrow(): Boolean {
    return checkInRange({
        precision(Calendar.DATE)

        add(Calendar.DATE, 1)
    }, {
        precision(Calendar.DATE)

        add(Calendar.DATE, 2)
        add(Calendar.SECOND, -1)
    })
}

fun Date.isYesterday(): Boolean {
    return checkInRange({
        precision(Calendar.DATE)

        add(Calendar.DATE, -1)
    }, {
        precision(Calendar.DATE)

        add(Calendar.SECOND, -1)
    })
}

fun Date.isThisWeek(): Boolean {
    return checkInRange({
        precision(Calendar.DATE)

        while (get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
            add(Calendar.DATE, -1)
    }, {
        precision(Calendar.DATE)

        while (get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
            add(Calendar.DATE, 1)
    })
}

fun Date.isNextWeek(): Boolean {
    return checkInRange({
        precision(Calendar.DATE)

        while (get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
            add(Calendar.DATE, 1)
    }, {
        precision(Calendar.DATE)

        add(Calendar.WEEK_OF_YEAR, 1)

        while (get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
            add(Calendar.DATE, 1)
    })
}

fun Date.weeksLeft(): Int {
    val startCalendar = Calendar.getInstance().apply {
        precision(Calendar.DATE)
    }

    val endCalendar = Calendar.getInstance().apply {
        time = this@weeksLeft

        precision(Calendar.DATE)
    }

    val startWeek = startCalendar.get(Calendar.WEEK_OF_YEAR)
    val endWeek = endCalendar.get(Calendar.WEEK_OF_YEAR)

    return endWeek - startWeek
}

fun Calendar.precision(vararg fields: Int): Calendar {
    return apply {
        (Calendar.HOUR_OF_DAY..Calendar.MILLISECOND)
                .subtract(fields.asIterable())
                .forEach { field -> set(field, 0) }
    }
}

fun Date.tomorrow(): Date {
    return Calendar.getInstance().let { calendar ->
        calendar.time = this

        calendar.precision(Calendar.DATE)

        calendar.add(Calendar.DATE, 1)

        Date(calendar.timeInMillis)
    }
}

fun Date.year() = Calendar.getInstance().let { calendar ->
    calendar.time = this

    calendar.get(Calendar.YEAR)
}

fun Date.dayOfWeekName() = Calendar.getInstance().let { calendar ->
    calendar.time = this

    calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, DEFAULT_LOCALE)!!
}

fun Date.checkInRange(startTransformation: Calendar.() -> Unit, endTransformation: Calendar.() -> Unit): Boolean {
    val start = Calendar.getInstance().apply(startTransformation).timeInMillis
    val end = Calendar.getInstance().apply(endTransformation).timeInMillis

    return (start..end).contains(time)
}