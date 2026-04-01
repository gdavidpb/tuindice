package com.gdavidpb.tuindice.data.source.reporting

class CrashReporterDataSource(
	private val setUserIdAction: (String) -> Unit,
	private val recordExceptionAction: (Throwable) -> Unit,
	private val logAction: (String) -> Unit,
	private val setIntKeyAction: (String, Int) -> Unit,
	private val setLongKeyAction: (String, Long) -> Unit,
	private val setFloatKeyAction: (String, Float) -> Unit,
	private val setDoubleKeyAction: (String, Double) -> Unit,
	private val setStringKeyAction: (String, String) -> Unit,
	private val setBooleanKeyAction: (String, Boolean) -> Unit
) {
	fun setUserId(identifier: String) {
		setUserIdAction(identifier)
	}

	fun recordException(throwable: Throwable) {
		recordExceptionAction(throwable)
	}

	fun log(message: String) {
		logAction(message)
	}

	fun setCustomKey(key: String, value: Int) {
		setIntKeyAction(key, value)
	}

	fun setCustomKey(key: String, value: Long) {
		setLongKeyAction(key, value)
	}

	fun setCustomKey(key: String, value: Float) {
		setFloatKeyAction(key, value)
	}

	fun setCustomKey(key: String, value: Double) {
		setDoubleKeyAction(key, value)
	}

	fun setCustomKey(key: String, value: String) {
		setStringKeyAction(key, value)
	}

	fun setCustomKey(key: String, value: Boolean) {
		setBooleanKeyAction(key, value)
	}
}
