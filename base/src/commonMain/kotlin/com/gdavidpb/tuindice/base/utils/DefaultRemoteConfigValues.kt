package com.gdavidpb.tuindice.base.utils

data class DefaultRemoteConfigValues(
	val timeoutMillis: Long,
	val contactEmail: String,
	val contactSubject: String,
	val loadingMessages: List<String>,
	val updateStalenessDays: Int,
	val syncsToSuggestReview: Int,
	val attestationAndroidEnforcementEnabled: Boolean,
	val attestationIosEnforcementEnabled: Boolean,
	val appAvailabilityNoticeEnabled: Boolean,
	val appAvailabilityNoticeTitle: String,
	val appAvailabilityNoticeMessage: String,
	val assistantMode: String
)
