package com.gdavidpb.tuindice.utils

import java.util.*

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
const val KEY_COOL_DOWN = "cooldown"
const val KEY_LAST_UPDATE = "lastUpdate"
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

/*
val QUARTER_STARTS = arrayOf(
        Calendar.JANUARY,
        Calendar.APRIL,
        Calendar.JULY,
        Calendar.SEPTEMBER)

val QUARTER_ENDS = arrayOf(
        Calendar.MARCH,
        Calendar.JULY,
        Calendar.AUGUST,
        Calendar.DECEMBER
)

val QUARTER_RANGES = QUARTER_STARTS.mapIndexed { index, _ ->
    QUARTER_STARTS[index] to QUARTER_ENDS[index]
}
*/

const val STATUS_QUARTER_CURRENT = 0
const val STATUS_QUARTER_COMPLETED = 1
const val STATUS_QUARTER_GUESS = 2
const val STATUS_QUARTER_RETIRED = 3

const val STATUS_SUBJECT_OK = 0
const val STATUS_SUBJECT_RETIRED = 1
const val STATUS_SUBJECT_NO_EFFECT = 2

const val STATE_EXPANDED = 0
const val STATE_COLLAPSED = 1
const val STATE_IDLE = 2

/* Default locale */
val DEFAULT_LOCALE: Locale = Locale("es", "VE")
val DEFAULT_TIME_ZONE: TimeZone = TimeZone.getTimeZone("America/Caracas")

/* Times */
const val TIME_COUNT_DOWN = 5 * 60 * 1000
const val TIME_BACKGROUND_ANIMATION = 30000L
const val TIME_DELAY_CLICK_ONCE = 500L

/* Summary view types */
const val VIEW_TYPE_SUMMARY_HEADER = 0
const val VIEW_TYPE_SUMMARY_SUBJECTS = 1
const val VIEW_TYPE_SUMMARY_CREDITS = 2

/* About view types */
const val VIEW_TYPE_ABOUT_HEADER = 0
const val VIEW_TYPE_ABOUT = 1
const val VIEW_TYPE_ABOUT_LIB = 2

/* Extras */
const val EXTRA_AWAITING_STATE = "EXTRA_AWAITING_STATE"
const val EXTRA_AWAITING_EMAIL = "EXTRA_AWAITING_EMAIL"

const val EXTRA_TITLE = "EXTRA_TITLE"
const val EXTRA_URL = "EXTRA_URL"

const val FLAG_RESET = 1000
const val FLAG_VERIFY = 1001