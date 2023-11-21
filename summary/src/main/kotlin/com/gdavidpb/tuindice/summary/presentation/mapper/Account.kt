package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.Account

fun Account.toShortName(): String {
	val firstName = firstNames.substringBefore(' ')
	val lastName = lastNames.substringBefore(' ')

	return "$firstName $lastName"
}