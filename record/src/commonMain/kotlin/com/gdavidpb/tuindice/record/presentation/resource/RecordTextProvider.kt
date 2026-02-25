package com.gdavidpb.tuindice.record.presentation.resource

interface RecordTextProvider {
	fun serviceUnavailable(): String
	fun networkUnavailable(): String
	fun timeout(): String
	fun defaultError(): String
}
