package com.gdavidpb.tuindice.data.source.crashlytics

import com.gdavidpb.tuindice.domain.repository.ReportingRepository
import com.google.firebase.crashlytics.FirebaseCrashlytics

open class CrashlyticsReportingDataStore(
        private val crashlytics: FirebaseCrashlytics
) : ReportingRepository {
    override fun setIdentifier(identifier: String) {
        crashlytics.setUserId(identifier)
    }

    override fun logException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    override fun <T : Any> setCustomKey(key: String, value: T) {
        when (value) {
            is Int -> crashlytics.setCustomKey(key, value)
            is Long -> crashlytics.setCustomKey(key, value)
            is Float -> crashlytics.setCustomKey(key, value)
            is Double -> crashlytics.setCustomKey(key, value)
            is String -> crashlytics.setCustomKey(key, value)
            is Boolean -> crashlytics.setCustomKey(key, value)
            else -> throw IllegalArgumentException("Unsupported value '$value' of type '${value::class.java.name}'")
        }
    }
}