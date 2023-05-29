package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.base.utils.extension.capitalize
import com.gdavidpb.tuindice.base.utils.extension.format
import com.gdavidpb.tuindice.base.utils.extension.isToday
import com.gdavidpb.tuindice.base.utils.extension.isYesterday
import java.util.Date
import java.util.concurrent.TimeUnit

fun Account.toShortName(): String {
	val firstName = firstNames.substringBefore(' ')
	val lastName = lastNames.substringBefore(' ')

	return "$firstName $lastName"
}

fun Date.formatLastUpdate(): String {
	val now = Date()
	val diff = now.time - time
	val days = TimeUnit.MILLISECONDS.toDays(diff)

	return when {
		time == 0L -> "Nunca"
		isToday() -> format("'Hoy,' hh:mm aa")
		isYesterday() -> format("'Ayer,' hh:mm aa")
		days < 7 -> format("EEEE',' hh:mm aa")
		else -> format("dd 'de' MMMM yyyy")
	}?.capitalize() ?: "-"
}