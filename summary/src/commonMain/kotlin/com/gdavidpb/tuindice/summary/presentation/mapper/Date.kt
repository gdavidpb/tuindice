package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.daysToNow
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.utils.extension.capitalize

fun Long.formatLastUpdate(): String {
	val daysDistance = daysToNow()

	return when {
		this == 0L -> "Nunca"
		daysDistance == 0 -> formatDate(DateTextStyle.TODAY_TIME)
		daysDistance == -1 -> formatDate(DateTextStyle.YESTERDAY_TIME)
		daysDistance < 7 -> formatDate(DateTextStyle.WEEKDAY_TIME)
		else -> formatDate(DateTextStyle.DAY_MONTH_YEAR)
	}?.capitalize() ?: "-"
}
