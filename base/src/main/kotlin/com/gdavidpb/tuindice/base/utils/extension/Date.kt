package com.gdavidpb.tuindice.base.utils.extension

import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

private val millisecondsPerDay = TimeUnit.DAYS.toMillis(1)
private val millisecondsPerWeek = TimeUnit.DAYS.toMillis(7)

fun Date.daysDistance(): Int {
	val distance = (time - System.currentTimeMillis()).toDouble()

	return ceil(distance / millisecondsPerDay).toInt()
}

fun Date.weeksDistance(): Int {
	val now = Calendar.getInstance()

	val target = Calendar.getInstance().apply {
		time = this@weeksDistance
		set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
	}

	val distance = (target.timeInMillis - now.timeInMillis).toDouble()

	return ceil(distance / millisecondsPerWeek).toInt()
}