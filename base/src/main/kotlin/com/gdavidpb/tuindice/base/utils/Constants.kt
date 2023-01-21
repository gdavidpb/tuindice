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

/* Subscription topics */
object Topics {
    const val TOPIC_GENERAL = "general"
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

/* Remote config keys */
object ConfigKeys {
    const val CONTACT_EMAIL = "contact_email"
    const val CONTACT_SUBJECT = "contact_subject"
    const val ISSUES_LIST = "issues_list"
    const val LOADING_MESSAGES = "loading_messages"
    const val TIME_UPDATE_STALENESS_DAYS = "time_update_staleness_days"
    const val SYNCS_TO_SUGGEST_REVIEW = "syncs_to_suggest_review"
    const val TIME_OUT_CONNECTION = "time_out_connection"
    const val TIME_OUT_SIGN_IN = "time_out_sign_in"
    const val TIME_OUT_GET_ENROLLMENT = "time_out_get_enrollment"
}

/* Reporting keys */
object ReportKeys {
    const val USE_CASE = "USE_CASE"
    const val HANDLED = "HANDLED"
}

/* Times */
const val TIME_EXIT_LOCKER = 2000L

/* Values */
const val MIN_EVALUATION_GRADE = 0.25
const val MAX_EVALUATION_GRADE = 100.0
const val MAX_QUARTER_GRADE = 5.0
const val MAX_SUBJECT_GRADE = 5

/* Status */
const val STATUS_QUARTER_CURRENT = 0
const val STATUS_QUARTER_COMPLETED = 1
const val STATUS_QUARTER_MOCK = 2
const val STATUS_QUARTER_RETIRED = 3

const val STATUS_SUBJECT_OK = 0
const val STATUS_SUBJECT_RETIRED = 1
const val STATUS_SUBJECT_NO_EFFECT = 2

/* Decimals */
const val DECIMALS_GRADE_SUBJECT = 2