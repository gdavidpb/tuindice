package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.base.utils.extension.capitalize
import com.gdavidpb.tuindice.base.utils.extension.daysDistance
import com.gdavidpb.tuindice.base.utils.extension.format
import java.util.Date

fun Account.toShortName(): String {
	val firstName = firstNames.substringBefore(' ')
	val lastName = lastNames.substringBefore(' ')

	return "$firstName $lastName"
}

fun Date.formatLastUpdate(): String {
	val daysDistance = daysDistance()

	return when {
		time == 0L -> "Nunca"
		daysDistance == 0L -> format("'Hoy,' hh:mm aa")
		daysDistance == -1L -> format("'Ayer,' hh:mm aa")
		daysDistance < 7 -> format("EEEE',' hh:mm aa")
		else -> format("dd 'de' MMMM yyyy")
	}?.capitalize() ?: "-"
}