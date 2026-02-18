package com.gdavidpb.tuindice.base.utils

import java.util.Locale

/* Default locale */
val DEFAULT_LOCALE: Locale = Locale("es", "VE")

/* Request codes */
object RequestCodes {
	const val APP_UPDATE = 1001
}

/* Preferences keys */
object PreferencesKeys {
	const val LAST_DESTINATION = "lastDestination"
	const val SYNCS_COUNTER = "syncsCounter"
	const val IS_SUBSCRIBED = "isSubscribed"
	const val USER_ACCESS_TOKEN = "accessToken"
	const val USER_REFRESH_TOKEN = "refreshToken"
	const val USER_USB_ID = "usbId"
}

/* Reporting keys */
object ReportKeys {
	const val USE_CASE = "useCase"
	const val IS_HANDLED = "isHandled"
}