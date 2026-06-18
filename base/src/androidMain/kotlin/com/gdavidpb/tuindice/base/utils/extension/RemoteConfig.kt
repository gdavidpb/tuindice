package com.gdavidpb.tuindice.base.utils.extension

import com.gdavidpb.tuindice.base.utils.DefaultRemoteConfigValues
import com.gdavidpb.tuindice.base.utils.RemoteConfigKeys
import org.json.JSONArray

fun DefaultRemoteConfigValues.toFirebaseDefaultsMap(): Map<String, Any> {
	return mapOf(
		RemoteConfigKeys.SYNCS_TO_SUGGEST_REVIEW to syncsToSuggestReview.toLong(),
		RemoteConfigKeys.TIME_UPDATE_STALENESS_DAYS to updateStalenessDays.toLong(),
		RemoteConfigKeys.CONTACT_EMAIL to contactEmail,
		RemoteConfigKeys.CONTACT_SUBJECT to contactSubject,
		RemoteConfigKeys.LOADING_MESSAGES to JSONArray(loadingMessages).toString(),
		RemoteConfigKeys.TIME_OUT_CONNECTION to timeoutMillis,
		RemoteConfigKeys.ATTESTATION_ANDROID_ENFORCEMENT_ENABLED to attestationAndroidEnforcementEnabled,
		RemoteConfigKeys.ATTESTATION_IOS_ENFORCEMENT_ENABLED to attestationIosEnforcementEnabled,
		RemoteConfigKeys.APP_AVAILABILITY_NOTICE_ENABLED to appAvailabilityNoticeEnabled,
		RemoteConfigKeys.APP_AVAILABILITY_NOTICE_TITLE to appAvailabilityNoticeTitle,
		RemoteConfigKeys.APP_AVAILABILITY_NOTICE_MESSAGE to appAvailabilityNoticeMessage
	)
}
