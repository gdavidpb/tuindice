package com.gdavidpb.tuindice.summary.utils

import kotlin.time.Duration.Companion.days

object PreferencesKeys {
	const val COOLDOWN_GET_ACCOUNT = "cooldownGetAccount"
}

object CooldownTimes {
	val COOLDOWN_GET_ACCOUNT = 1.days.inWholeMilliseconds
}