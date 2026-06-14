package com.gdavidpb.tuindice.evaluations.presentation.utils

import com.gdavidpb.tuindice.base.utils.currentTimeMillis

fun Long?.isDateInPast(): Boolean {
	return this != null && this < currentTimeMillis()
}
