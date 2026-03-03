package com.gdavidpb.tuindice.data

import com.gdavidpb.tuindice.base.logging.appLogger
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository

class DebugReportingDataSource : ReportingRepository {
	private val logger = appLogger(tag = "Reporting")

	override fun setIdentifier(identifier: String) {
		logger.d { "setIdentifier(identifier=$identifier)" }
	}

	override fun logException(throwable: Throwable) {
		logger.e(throwable) { "ReportingRepository.logException()" }
	}

	override fun logMessage(message: String) {
		logger.i { message }
	}

	override fun <T : Any> setCustomKey(key: String, value: T) {
		logger.d { "setCustomKey(key=$key, value=$value, type=${value::class.java.name})" }
	}
}
