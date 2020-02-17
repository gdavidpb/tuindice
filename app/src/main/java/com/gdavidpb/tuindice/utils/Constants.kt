package com.gdavidpb.tuindice.utils

import java.util.*

const val NO_GETTER = "Property does not have a getter"

/* External packages */
const val PACKAGE_NAME_WEB_VIEW = "com.google.android.webview"

/* Third-party urls */
const val URL_CREATIVE_COMMONS = "https://creativecommons.org/licenses/by-nc/4.0/"
const val URL_PRIVACY_POLICY = "https://tuindice-usb.firebaseapp.com/"
const val URL_TWITTER = "https://twitter.com/TuIndice"

/* Emails */
const val EMAIL_CONTACT = "tuindice@gmail.com"
const val EMAIL_SUBJECT_CONTACT = "TuIndice - Contacto"

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
const val MAX_GRADE = 5

/* Status */
const val STATUS_QUARTER_CURRENT = 0
const val STATUS_QUARTER_COMPLETED = 1
const val STATUS_QUARTER_GUESS = 2
const val STATUS_QUARTER_RETIRED = 3

const val STATUS_SUBJECT_OK = 0
const val STATUS_SUBJECT_RETIRED = 1
const val STATUS_SUBJECT_NO_EFFECT = 2
const val STATUS_SUBJECT_GAVE_UP = 3

/* Default locale */
val DEFAULT_LOCALE: Locale = Locale("es", "VE")
val DEFAULT_TIME_ZONE: TimeZone = TimeZone.getTimeZone("America/Caracas")

/* Times */
const val TIME_COUNT_DOWN = 5 * 60 * 1000
const val TIME_BACKGROUND_ANIMATION = 30000L
const val TIME_DELAY_CLICK_ONCE = 500L
const val TIME_OUT_CONNECTION = 90000L

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

const val EXTRA_TITLE = "EXTRA_TITLE"
const val EXTRA_URL = "EXTRA_URL"

/* Reporting keys */
const val KEY_REF_DATE = "KEY_REF_DATE"
const val KEY_NOW_DATE = "KEY_NOW_DATE"

/* Navigation args */

const val ARG_SUBJECT_ID = "subjectId"

const val FLAG_RESET = 1000
const val FLAG_VERIFY = 1001

const val REF_BASE = 2000