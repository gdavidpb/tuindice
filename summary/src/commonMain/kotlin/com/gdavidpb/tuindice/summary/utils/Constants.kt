package com.gdavidpb.tuindice.summary.utils

import kotlin.time.Duration.Companion.days

object PreferencesKeys {
	const val COOLDOWN_GET_USER = "cooldownGetUser"
}

object CooldownTimes {
	val COOLDOWN_GET_USER = 1.days.inWholeMilliseconds
}