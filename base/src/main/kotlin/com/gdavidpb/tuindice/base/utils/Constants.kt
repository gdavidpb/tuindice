package com.gdavidpb.tuindice.base.utils

import java.util.Locale
import java.util.TimeZone

/* Default locale */
val DEFAULT_LOCALE: Locale = Locale("es", "VE")
val DEFAULT_TIME_ZONE: TimeZone = TimeZone.getTimeZone("America/Caracas")

/* Errors */
const val NO_GETTER = "Property does not have a getter."

/* Request codes */
object RequestCodes {
	const val APP_UPDATE = 1001
	const val PLAY_SERVICES_RESOLUTION = 2404
}

/* Preferences keys */
object PreferencesKeys {
	const val LAST_SCREEN = "lastScreen"
	const val SYNCS_COUNTER = "syncsCounter"
	const val SUBSCRIBED_TOPICS = "subscribedTopics"
	const val ACTIVE_TOKEN = "activeToken"
}

/* Reporting keys */
object ReportKeys {
	const val USE_CASE = "useCase"
	const val IS_HANDLED = "isHandled"
}

/* Status */
const val STATUS_QUARTER_CURRENT = 0
const val STATUS_QUARTER_COMPLETED = 1
const val STATUS_QUARTER_MOCK = 2
const val STATUS_QUARTER_RETIRED = 3

const val STATUS_SUBJECT_OK = 0
const val STATUS_SUBJECT_RETIRED = 1
const val STATUS_SUBJECT_NO_EFFECT = 2