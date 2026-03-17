package com.gdavidpb.tuindice.data.source.reporting

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.platform.IosObservabilityCapability

class IosReportingDataSource(
	private val observabilityCapability: IosObservabilityCapability
) : ReportingRepository {
	override fun setIdentifier(identifier: String) {
		observabilityCapability.setUserIdentifier(identifier)
	}

	override fun logException(throwable: Throwable) {
		observabilityCapability.logException(throwable)
	}

	override fun logMessage(message: String) {
		observabilityCapability.logMessage(message)
	}

	override fun <T : Any> setCustomKey(key: String, value: T) {
		observabilityCapability.setCustomKey(key, value.toString())
	}
}
