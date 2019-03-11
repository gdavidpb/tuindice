package com.gdavidpb.tuindice.utils

import java.util.*

/* Dst service endpoints */
const val ENDPOINT_DST_SECURE = "https://secure.dst.usb.ve/"
const val ENDPOINT_DST_RECORD = "https://expediente.dii.usb.ve/"
const val ENDPOINT_DST_ENROLLMENT = "https://comprobante.dii.usb.ve/"
const val ENDPOINT_DST_POLL = "https://consulta.dii.usb.ve/"

const val ENDPOINT_DST_RECORD_AUTH = "${ENDPOINT_DST_RECORD}login.do"
const val ENDPOINT_DST_ENROLLMENT_AUTH = "${ENDPOINT_DST_ENROLLMENT}CAS/login.do"
const val ENDPOINT_DST_POLL_AUTH = "${ENDPOINT_DST_ENROLLMENT}Consulta/Secure/zk/login.zul"

/* Firebase storage urls */
const val URL_BASE = "tuindice-usb.firebaseapp.com"
const val URL_PRIVACY_POLICY = "https://$URL_BASE/"

/* Preferences keys */
const val KEY_USB_ID = "email"
const val KEY_PASSWORD = "password"
const val KEY_COUNT_DOWN = "countdown"
const val KEY_COOL_DOWN = "cooldown"
const val KEY_LAST_UPDATE = "lastUpdate"
const val KEY_AWAITING_EMAIL = "AwaitingEmail"
const val KEY_AWAITING_PASSWORD = "AwaitingPassword"

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

const val STATUS_QUARTER_CURRENT = 0
const val STATUS_QUARTER_COMPLETED = 1
const val STATUS_QUARTER_GUESS = 2
const val STATUS_QUARTER_RETIRED = 3

const val STATUS_SUBJECT_OK = 0
const val STATUS_SUBJECT_RETIRED = 1
const val STATUS_QUARTER_NO_EFFECT = 2

/* Default locale */
val DEFAULT_LOCALE = Locale("es", "VE")

/* Times */
const val TIME_COUNT_DOWN = 5 * 60 * 1000
const val TIME_BACKGROUND_ANIMATION = 30000L
const val TIME_DELAY_CLICK_ONCE = 500L

/* Summary view types */
const val VIEW_TYPE_SUMMARY_HEADER = 0
const val VIEW_TYPE_SUMMARY_SUBJECTS = 1
const val VIEW_TYPE_SUMMARY_CREDITS = 2

/* Extras */
const val EXTRA_AWAITING_STATE = "EXTRA_AWAITING_STATE"
const val EXTRA_AWAITING_EMAIL = "EXTRA_AWAITING_EMAIL"

const val FLAG_RESET = 1000
const val FLAG_VERIFY = 1001