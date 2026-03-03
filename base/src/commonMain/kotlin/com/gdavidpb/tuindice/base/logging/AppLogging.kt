package com.gdavidpb.tuindice.base.logging

import co.touchlab.kermit.Logger

private const val APP_LOG_TAG = "TuIndice"

fun appLogger(tag: String = APP_LOG_TAG): Logger {
	return Logger.withTag(tag)
}
