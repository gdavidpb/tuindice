package com.gdavidpb.tuindice.base.utils

import java.util.*

/* Default locale */
val DEFAULT_LOCALE: Locale = Locale("es", "VE")
val DEFAULT_TIME_ZONE: TimeZone = TimeZone.getTimeZone("America/Caracas")

/* Concurrent locks */
object Locks {
	const val GET_ACCOUNT_AND_QUARTERS = "getAccountAndQuarters"
}

/* Errors */
const val NO_GETTER = "Property does not have a getter."

/* Request codes */
object RequestCodes {
	const val APP_UPDATE_REQUEST = 1001
	const val PLAY_SERVICES_RESOLUTION_REQUEST = 2404
}

/* Main screen keys */
object ScreenKeys {
	const val SUMMARY = 0
	const val RECORD = 1
	const val ABOUT = 2
}

/* Preferences keys */
object SettingsKeys {
	const val LAST_SCREEN = "lastScreen"
	const val SYNCS_COUNTER = "syncsCounter"
	const val SUBSCRIBED_TOPICS = "subscribedTopics"
	const val ACTIVE_TOKEN = "activeToken"
}

/* Reporting keys */
object ReportKeys {
	const val USE_CASE = "USE_CASE"
	const val HANDLED = "HANDLED"
}

/* Times */
const val TIME_EXIT_LOCKER = 2000L

/* Status */
const val STATUS_QUARTER_CURRENT = 0
const val STATUS_QUARTER_COMPLETED = 1
const val STATUS_QUARTER_MOCK = 2
const val STATUS_QUARTER_RETIRED = 3

const val STATUS_SUBJECT_OK = 0
const val STATUS_SUBJECT_RETIRED = 1
const val STATUS_SUBJECT_NO_EFFECT = 2