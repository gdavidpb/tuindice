package com.gdavidpb.tuindice.utils.extensions

import java.util.*

fun Date.isToday(): Boolean {
    val start = Calendar.getInstance().run {
        precision(Calendar.DATE)

        Date(timeInMillis)
    }

    val end = Calendar.getInstance().run {
        precision(Calendar.DATE)

        add(Calendar.DATE, 1)
        add(Calendar.SECOND, -1)

        Date(timeInMillis)
    }

    return (start..end).contains(this)
}

fun Date.isYesterday(): Boolean {
    val start = Calendar.getInstance().run {
        precision(Calendar.DATE)

        add(Calendar.DATE, -1)

        Date(timeInMillis)
    }

    val end = Calendar.getInstance().run {
        precision(Calendar.DATE)

        add(Calendar.SECOND, -1)

        Date(timeInMillis)
    }

    return (start..end).contains(this)
}

fun Calendar.precision(vararg fields: Int): Calendar {
    return apply {
        (Calendar.HOUR_OF_DAY..Calendar.MILLISECOND)
                .subtract(fields.asIterable())
                .forEach { field -> set(field, 0) }
    }
}

fun Calendar.containsInMonth(value: Calendar): Boolean {
    val start = Calendar.getInstance().let {
        it.time = this.time

        precision(Calendar.DAY_OF_MONTH)

        set(Calendar.DAY_OF_MONTH, 1)

        Date(it.timeInMillis)
    }

    val end = Calendar.getInstance().let {
        it.time = start

        it.add(Calendar.MONTH, 1)

        Date(it.timeInMillis)
    }

    return (start..end).contains(value.time)
}

fun Date.tomorrow(): Date {
    return Calendar.getInstance().let {
        it.time = this

        it.precision(Calendar.DATE)

        it.add(Calendar.DATE, 1)

        Date(it.timeInMillis)
    }
}