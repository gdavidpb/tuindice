package com.gdavidpb.tuindice.utils.extensions

import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

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
    val days = TimeUnit.MILLISECONDS.toDays(time - Date().time)

    return ceil(days / 7f).toInt()
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

fun Date.get(field: Int) = Calendar.getInstance().let { calendar ->
    calendar.time = this

    calendar.get(field)
}

fun Date.add(field: Int, value: Int) = Calendar.getInstance().let { calendar ->
    calendar.time = this

    calendar.add(field, value)

    Date(calendar.timeInMillis)
}

fun Date.checkInRange(startTransformation: Calendar.() -> Unit, endTransformation: Calendar.() -> Unit): Boolean {
    val start = Calendar.getInstance().apply(startTransformation).timeInMillis
    val end = Calendar.getInstance().apply(endTransformation).timeInMillis

    return (start..end).contains(time)
}