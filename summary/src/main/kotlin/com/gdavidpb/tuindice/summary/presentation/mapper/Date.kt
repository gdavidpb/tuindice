package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.mapper.daysToNow
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.utils.extension.capitalize

fun Long.formatLastUpdate(): String {
	val daysDistance = daysToNow()

	return when {
		this == 0L -> "Nunca"
		daysDistance == 0 -> formatDate("'Hoy,' hh:mm aa")
		daysDistance == -1 -> formatDate("'Ayer,' hh:mm aa")
		daysDistance < 7 -> formatDate("EEEE',' hh:mm aa")
		else -> formatDate("dd 'de' MMMM yyyy")
	}?.capitalize() ?: "-"
}