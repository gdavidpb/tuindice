package com.gdavidpb.tuindice.data.utils

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
const val URL_PRIVACY_POLICY = "gs://tuindice-usb.appspot.com/privacy_policy.html"

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

/* Default locale */
val DEFAULT_LOCALE = Locale("es", "VE")

/* Result codes */
const val RES_UPDATE = 0
const val RES_RESPONSE = 1

/* Notification channels */
const val CHANNEL_SERVICE = "CHANNEL_SERVICE"

/* Preferences keys */
const val KEY_FIRST_RUN = "KEY_FIRST_RUN"
const val KEY_REFRESH_COOLDOWN = "KEY_REFRESH_COOLDOWN"

/* Extras */
const val EXTRA_UPDATE = "EXTRA_UPDATE"
const val EXTRA_ACCOUNT = "EXTRA_ACCOUNT"
const val EXTRA_RESPONSE = "EXTRA_RESPONSE"
const val EXTRA_RECEIVER = "EXTRA_RECEIVER"

/* Times */
const val TIME_BACKGROUND_ANIMATION = 30000L
const val TIME_DELAY_CLICK_ONCE = 500L