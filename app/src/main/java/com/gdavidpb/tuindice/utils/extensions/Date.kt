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
        add(Calendar.MILLISECOND, -1)
    })
}

fun Date.isTomorrow(): Boolean {
    return checkInRange({
        precision(Calendar.DATE)

        add(Calendar.DATE, 1)
    }, {
        precision(Calendar.DATE)

        add(Calendar.DATE, 2)
        add(Calendar.MILLISECOND, -1)
    })
}

fun Date.isYesterday(): Boolean {
    return checkInRange({
        precision(Calendar.DATE)

        add(Calendar.DATE, -1)
    }, {
        precision(Calendar.DATE)

        add(Calendar.MILLISECOND, -1)
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

        add(Calendar.DATE, 1)
        add(Calendar.MILLISECOND, -1)
    })
}

fun Date.isNextWeek(): Boolean {
    return checkInRange({
        precision(Calendar.DATE)

        add(Calendar.WEEK_OF_YEAR, 1)

        while (get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
            add(Calendar.DATE, -1)
    }, {
        precision(Calendar.DATE)

        add(Calendar.WEEK_OF_YEAR, 1)

        while (get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
            add(Calendar.DATE, 1)

        add(Calendar.DATE, 1)
        add(Calendar.MILLISECOND, -1)
    })
}

fun Date.weeksLeft(): Int {
    val days = TimeUnit.MILLISECONDS.toDays(time - Date().time)

    return ceil((days + 1) / 7f).toInt()
}

fun Calendar.precision(vararg fields: Int): Calendar {
    return apply {
        (Calendar.HOUR_OF_DAY..Calendar.MILLISECOND)
                .subtract(fields.asIterable())
                .forEach { field -> set(field, 0) }
    }
}

fun Date.tomorrow(): Date {
    return Calendar.getInstance().run {
        time = this@tomorrow

        precision(Calendar.DATE)

        add(Calendar.DATE, 1)

        time
    }
}

fun Date.add(field: Int, value: Int): Date = Calendar.getInstance().run {
    time = this@add

    add(field, value)

    time
}

fun Date.get(field: Int): Int = Calendar.getInstance().run {
    time = this@get

    get(field)
}

private fun Date.checkInRange(startTransformation: Calendar.() -> Unit, endTransformation: Calendar.() -> Unit): Boolean {
    val start = Calendar.getInstance().apply(startTransformation).time
    val end = Calendar.getInstance().apply(endTransformation).time

    return this in start..end
}