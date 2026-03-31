package com.gdavidpb.tuindice.data.source.reporting

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.data.contract.reporting.CrashReporterDataSource

class CrashlyticsReportingDataSource(
	private val crashReporter: CrashReporterDataSource
) : ReportingRepository {
	override fun setIdentifier(identifier: String) {
		crashReporter.setUserId(identifier)
	}

	override fun logException(throwable: Throwable) {
		crashReporter.recordException(throwable)
	}

	override fun logMessage(message: String) {
		crashReporter.log(message)
	}

	override fun <T : Any> setCustomKey(key: String, value: T) {
		when (value) {
			is Int -> crashReporter.setCustomKey(key, value)
			is Long -> crashReporter.setCustomKey(key, value)
			is Float -> crashReporter.setCustomKey(key, value)
			is Double -> crashReporter.setCustomKey(key, value)
			is String -> crashReporter.setCustomKey(key, value)
			is Boolean -> crashReporter.setCustomKey(key, value)
			else -> throw IllegalArgumentException(
				"Unsupported value '$value' of type '${value::class.java.name}'"
			)
		}
	}
}
