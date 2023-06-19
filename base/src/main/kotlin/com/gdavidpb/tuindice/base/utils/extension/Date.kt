package com.gdavidpb.tuindice.base.utils.extension

import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.round

fun Date.daysDistance(): Int {
	val now = Calendar.getInstance()

	val target = Calendar.getInstance().apply {
		time = this@daysDistance
	}

	val millisecondsPerDay = TimeUnit.DAYS.toMillis(1)
	val distance = (target.timeInMillis - now.timeInMillis).toDouble()

	return round(distance / millisecondsPerDay).toInt()
}

fun Date.weeksDistance(): Int {
	val now = Calendar.getInstance()

	val target = Calendar.getInstance().apply {
		time = this@weeksDistance
		set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
	}

	val millisecondsPerWeek = TimeUnit.DAYS.toMillis(7)
	val distance = (target.timeInMillis - now.timeInMillis).toDouble()

	return round(distance / millisecondsPerWeek).toInt()
}