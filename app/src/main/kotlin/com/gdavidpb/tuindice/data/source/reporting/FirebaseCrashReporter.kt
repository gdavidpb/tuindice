package com.gdavidpb.tuindice.data.source.reporting

import com.google.firebase.crashlytics.FirebaseCrashlytics

class FirebaseCrashReporter(
	private val crashlytics: FirebaseCrashlytics
) : CrashReporter {
	override fun setUserId(identifier: String) {
		crashlytics.setUserId(identifier)
	}

	override fun recordException(throwable: Throwable) {
		crashlytics.recordException(throwable)
	}

	override fun log(message: String) {
		crashlytics.log(message)
	}

	override fun setCustomKey(key: String, value: Int) {
		crashlytics.setCustomKey(key, value)
	}

	override fun setCustomKey(key: String, value: Long) {
		crashlytics.setCustomKey(key, value)
	}

	override fun setCustomKey(key: String, value: Float) {
		crashlytics.setCustomKey(key, value)
	}

	override fun setCustomKey(key: String, value: Double) {
		crashlytics.setCustomKey(key, value)
	}

	override fun setCustomKey(key: String, value: String) {
		crashlytics.setCustomKey(key, value)
	}

	override fun setCustomKey(key: String, value: Boolean) {
		crashlytics.setCustomKey(key, value)
	}
}
