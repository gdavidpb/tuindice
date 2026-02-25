package com.gdavidpb.tuindice.base.domain.repository

interface ReportingGateway {
	fun setIdentifier(identifier: String)
	fun logException(throwable: Throwable)
	fun logMessage(message: String)

	fun <T : Any> setCustomKey(key: String, value: T)
}
