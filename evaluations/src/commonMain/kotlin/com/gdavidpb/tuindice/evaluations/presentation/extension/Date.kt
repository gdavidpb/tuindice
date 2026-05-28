package com.gdavidpb.tuindice.evaluations.presentation.extension

import com.gdavidpb.tuindice.base.utils.currentTimeMillis

fun Long?.isDateInPast(): Boolean {
	return this != null && this < currentTimeMillis()
}
