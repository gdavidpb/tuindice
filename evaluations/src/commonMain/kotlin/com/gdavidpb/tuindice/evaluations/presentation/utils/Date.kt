@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.evaluations.presentation.utils

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

fun Long?.isDateInPast(clock: Clock): Boolean {
	return this != null && this < clock.now().toEpochMilliseconds()
}
