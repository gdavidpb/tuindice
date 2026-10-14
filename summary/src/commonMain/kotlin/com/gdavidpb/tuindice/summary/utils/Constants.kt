package com.gdavidpb.tuindice.summary.utils

import kotlin.time.Duration.Companion.days

object PreferencesKeys {
	const val COOLDOWN_GET_USER = "cooldownGetUser"
	const val PROFILE_PICTURE_VERSION = "profilePictureVersion"
	const val SHARE_BACKGROUND_ID = "shareBackgroundId"
	const val SHARE_BACKGROUNDS_CATALOG = "shareBackgroundsCatalog"
}

object CooldownTimes {
	val COOLDOWN_GET_USER = 1.days.inWholeMilliseconds
}