package com.gdavidpb.tuindice.summary.presentation.resource

interface SummaryTextProvider {
	fun lastUpdate(lastUpdate: Long): String
	fun serviceUnavailable(): String
	fun networkUnavailable(): String
	fun timeout(): String
	fun noService(): String
	fun defaultError(): String
	fun profilePictureUpdated(): String
	fun profilePictureRemoved(): String
}
