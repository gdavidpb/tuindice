package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.User

fun User.toShortName(): String {
	val firstName = firstNames.substringBefore(' ')
	val lastName = lastNames.substringBefore(' ')

	return "$firstName $lastName"
}