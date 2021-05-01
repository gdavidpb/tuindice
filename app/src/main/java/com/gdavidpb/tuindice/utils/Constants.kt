package com.gdavidpb.tuindice.utils

import java.util.*

/* Default locale */
val DEFAULT_LOCALE: Locale = Locale("es", "VE")
val DEFAULT_TIME_ZONE: TimeZone = TimeZone.getTimeZone("America/Caracas")

/* Errors */
const val NO_GETTER = "Property does not have a getter"

/* Actions */
object Actions {
    const val REMOVE_PROFILE_PICTURE = "com.gdavidpb.tuindice.intent.action.ACTION_REMOVE_PROFILE_PICTURE"
}

/* Request codes */
object RequestCodes {
    const val PROFILE_PICTURE_REQUEST = 1000
    const val APP_UPDATE_REQUEST = 1001
    const val PLAY_SERVICES_RESOLUTION_REQUEST = 2404
}

/* Extras */
object Extras {
    const val REMOVE_PROFILE_PICTURE = "REMOVE_PROFILE_PICTURE"
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
    const val USB_ID = "email"
    const val PASSWORD = "password"
    const val COUNT_DOWN = "countdown"
    const val LAST_SCREEN = "lastScreen"
    const val SYNCS_COUNTER = "syncsCounter"
    const val SUBSCRIBED_TOPICS = "subscribedTopics"
}

/* Remote config keys */
object ConfigKeys {
    const val CONTACT_EMAIL = "contact_email"
    const val CONTACT_SUBJECT = "contact_subject"
    const val ISSUES_LIST = "issues_list"
    const val LOADING_MESSAGES = "loading_messages"
    const val DST_CERTIFICATES = "dst_certificates"
    const val TIME_VERIFICATION_COUNT_DOWN = "time_verification_count_down"
    const val TIME_UPDATE_STALENESS_DAYS = "time_update_staleness_days"
    const val SYNCS_TO_SUGGEST_REVIEW = "syncs_to_suggest_review"
    const val TIME_OUT_CONNECTION = "time_out_connection"
    const val TIME_OUT_SIGN_IN = "time_out_sign_in"
    const val TIME_OUT_SYNC = "time_out_sync"
    const val TIME_OUT_GET_ENROLLMENT = "time_out_get_enrollment"
    const val TIME_OUT_PROFILE_PICTURE = "time_out_profile_picture"
    const val TIME_OUT_UPDATE_PASSWORD = "time_out_update_password"
    const val TIME_OUT_RESET_PASSWORD = "time_out_reset_password"
}

/* Reporting keys */
object ReportKeys {
    const val USE_CASE = "USE_CASE"
    const val HANDLED = "HANDLED"
}

/* Times */
const val TIME_EXIT_LOCKER = 2000L

/* Values */
const val MAX_QUARTER_GRADE = 5.0
const val MAX_SUBJECT_GRADE = 5
const val MAX_EVALUATION_GRADE = 100.0
const val MAX_EVALUATION_NOTES = 32
const val MAX_MULTIPLE_EVENT = 2

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

const val DECIMALS_DIV = 0.25