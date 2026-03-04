package com.gdavidpb.tuindice.base.data.source.reporting

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.logging.appLogger

class DebugReportingDataSource(
	private val sourceName: String
) : ReportingRepository {
	private val logger = appLogger(tag = "Reporting")

	override fun setIdentifier(identifier: String) {
		logger.d { "[$sourceName] setIdentifier(identifier=$identifier)" }
	}

	override fun logException(throwable: Throwable) {
		logger.e(throwable) { "[$sourceName] logException()" }
	}

	override fun logMessage(message: String) {
		logger.i { "[$sourceName] $message" }
	}

	override fun <T : Any> setCustomKey(key: String, value: T) {
		val valueType = value::class.simpleName ?: "unknown"
		logger.d { "[$sourceName] setCustomKey(key=$key, value=$value, type=$valueType)" }
	}
}
