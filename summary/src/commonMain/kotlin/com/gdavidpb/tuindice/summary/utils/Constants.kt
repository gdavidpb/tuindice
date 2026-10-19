package com.gdavidpb.tuindice.summary.utils

import com.gdavidpb.tuindice.base.domain.model.ImageEncodingSpec
import kotlin.time.Duration.Companion.days

object PreferencesKeys {
	const val COOLDOWN_GET_USER = "cooldownGetUser"
	const val PROFILE_PICTURE_VERSION = "profilePictureVersion"
	const val SHARE_BACKGROUND_ID = "shareBackgroundId"
	const val SHARE_BACKGROUNDS_CATALOG = "shareBackgroundsCatalog"
	const val SEEN_TERM_REPORT_IDS = "seenTermReportIds"
	const val TERM_REPORT_FEEDBACKS = "termReportFeedbacks"
}

object CooldownTimes {
	val COOLDOWN_GET_USER = 1.days.inWholeMilliseconds
}

object ProfilePictureEncoding {
	// The profile picture as the server takes it: at most 1024 px on its longest side and 1 MB, as JPEG of quality 85.
	val SPEC = ImageEncodingSpec(maxDimensionPx = 1024, maxBytes = 1_048_576, jpegQuality = 85)
}
