package com.gdavidpb.tuindice.utils

import java.util.*

/* Errors */
const val NO_GETTER = "Property does not have a getter"
const val NO_SETTER = "Property does not have a setter"

/* Actions */
const val ACTION_REMOVE_PROFILE_PICTURE = "com.gdavidpb.tuindice.intent.action.ACTION_REMOVE_PROFILE_PICTURE"

/* Request */
const val REQUEST_CODE_PROFILE_PICTURE = 1000

/* Headers */
const val HEADER_DATE = "Date"

/* Preferences keys */
const val KEY_USB_ID = "email"
const val KEY_PASSWORD = "password"
const val KEY_COUNT_DOWN = "countdown"
const val KEY_LAST_SCREEN = "lastScreen"
const val KEY_SYNCS_COUNTER = "syncsCounter"

/* Remote config keys */
const val KEY_CONTACT_EMAIL = "contact_email"
const val KEY_CONTACT_SUBJECT = "contact_subject"
const val KEY_ISSUES_LIST = "issues_list"
const val KEY_LOADING_MESSAGES = "loading_messages"
const val KEY_DST_CERTIFICATES = "dst_certificates"
const val KEY_TIME_VERIFICATION_COUNT_DOWN = "time_verification_count_down"
const val KEY_TIME_OUT_CONNECTION = "time_out_connection"
const val KEY_TIME_SYNCHRONIZATION = "time_synchronization"
const val KEY_SYNCS_TO_SUGGEST_REVIEW = "syncs_to_suggest_review"

/* Romans */
val ROMANS = hashMapOf(
        "1" to "I",
        "2" to "II",
        "3" to "III",
        "4" to "IV",
        "5" to "V",
        "6" to "VI",
        "7" to "VII",
        "8" to "VIII",
        "9" to "IX",
        "10" to "X"
)

/* Google */
const val PLAY_SERVICES_RESOLUTION_REQUEST = 2404

/* Times */
const val TIME_EXIT_LOCKER = 2000L

/* Values */
const val MAX_SUBJECT_GRADE = 5
const val MAX_EVALUATION_GRADE = 100.0
const val MAX_MULTIPLE_EVENT = 2

const val SAMPLE_PROFILE_PICTURE = 1024
const val QUALITY_PROFILE_PICTURE = 90

const val DEFAULT_SHRED_PASSES = 35

/* Status */
const val STATUS_QUARTER_CURRENT = 0
const val STATUS_QUARTER_COMPLETED = 1
const val STATUS_QUARTER_GUESS = 2
const val STATUS_QUARTER_RETIRED = 3

const val STATUS_SUBJECT_OK = 0
const val STATUS_SUBJECT_RETIRED = 1
const val STATUS_SUBJECT_NO_EFFECT = 2

/* Default locale */
val DEFAULT_LOCALE: Locale = Locale("es", "VE")
val DEFAULT_TIME_ZONE: TimeZone = TimeZone.getTimeZone("America/Caracas")

/* Summary view types */
const val VIEW_TYPE_SUMMARY_SUBJECTS = 0
const val VIEW_TYPE_SUMMARY_CREDITS = 1

/* About view types */
const val VIEW_TYPE_ABOUT_HEADER = 0
const val VIEW_TYPE_ABOUT = 1

/* Extras */
const val EXTRA_REMOVE_PROFILE_PICTURE = "EXTRA_REMOVE_PROFILE_PICTURE"

/* Reporting keys */
const val KEY_USE_CASE = "KEY_USE_CASE"
const val KEY_HANDLED = "KEY_HANDLED"

const val FLAG_RESET = 1000
const val FLAG_VERIFY = 1001

const val REF_BASE = 2000

/* Decimals */
const val DECIMALS_GRADE_SUBJECT = 2
const val DECIMALS_GRADE_QUARTER = 4

const val DECIMALS_DIV = 0.25