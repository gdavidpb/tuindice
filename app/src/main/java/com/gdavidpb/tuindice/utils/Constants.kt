package com.gdavidpb.tuindice.utils

import java.util.*

/* Errors */
const val NO_GETTER = "Property does not have a getter"
const val NO_SETTER = "Property does not have a setter"

/* Actions */
const val ACTION_REMOVE_PROFILE_PICTURE = "com.gdavidpb.tuindice.intent.action.ACTION_REMOVE_PROFILE_PICTURE"

/* Request */
const val REQUEST_CODE_PROFILE_PICTURE = 1000

/* External packages */
const val PACKAGE_NAME_WEB_VIEW = "com.google.android.webview"

/* Third-party urls */
const val URL_CREATIVE_COMMONS = "https://creativecommons.org/licenses/by-nc/4.0/"
const val URL_PRIVACY_POLICY = "https://tu-indice-usb.firebaseapp.com/"
const val URL_TWITTER = "https://twitter.com/TuIndice"

/* Emails */
const val EMAIL_CONTACT = "tuindice@gmail.com"
const val EMAIL_SUBJECT_CONTACT = "TuIndice - Contacto"

/* Headers */
const val HEADER_DATE = "Date"

/* Preferences keys */
const val KEY_USB_ID = "email"
const val KEY_PASSWORD = "password"
const val KEY_COUNT_DOWN = "countdown"
const val KEY_AWAITING_EMAIL = "awaitingEmail"
const val KEY_AWAITING_PASSWORD = "awaitingPassword"
const val KEY_LAST_SCREEN = "lastScreen"

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

/* Values */
const val MAX_SUBJECT_GRADE = 5
const val MAX_EVALUATION_GRADE = 100.0
const val MAX_MULTIPLE_EVENT = 2

const val SAMPLE_PROFILE_PICTURE = 1024
const val QUALITY_PROFILE_PICTURE = 90

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

/* Times */
const val TIME_COUNT_DOWN = 5 * 60 * 1000
const val TIME_BACKGROUND_ANIMATION = 30 * 1000L
const val TIME_DELAY_CLICK_ONCE = 500L
const val TIME_OUT_CONNECTION = 90 * 1000L
const val TIME_SYNCHRONIZATION = 1 * 60 * 60 * 1000L

/* Summary view types */
const val VIEW_TYPE_SUMMARY_SUBJECTS = 0
const val VIEW_TYPE_SUMMARY_CREDITS = 1

/* About view types */
const val VIEW_TYPE_ABOUT_HEADER = 0
const val VIEW_TYPE_ABOUT = 1

/* Extras */
const val EXTRA_AWAITING_STATE = "EXTRA_AWAITING_STATE"
const val EXTRA_AWAITING_EMAIL = "EXTRA_AWAITING_EMAIL"
const val EXTRA_FIRST_START_UP = "EXTRA_FIRST_START_UP"
const val EXTRA_REMOVE_PROFILE_PICTURE = "EXTRA_REMOVE_PROFILE_PICTURE"

const val EXTRA_TITLE = "EXTRA_TITLE"
const val EXTRA_URL = "EXTRA_URL"

/* Reporting keys */
const val KEY_USE_CASE = "KEY_USE_CASE"

/* Navigation args */
const val ARG_SUBJECT_ID = "subjectId"

const val FLAG_RESET = 1000
const val FLAG_VERIFY = 1001

const val REF_BASE = 2000

/* Decimals */
const val DECIMALS_GRADE_SUBJECT = 2
const val DECIMALS_GRADE_QUARTER = 4

const val DECIMALS_DIV = 0.25