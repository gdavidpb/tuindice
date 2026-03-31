package com.gdavidpb.tuindice.data.contract.reporting

interface CrashReporterDataSource {
	fun setUserId(identifier: String)
	fun recordException(throwable: Throwable)
	fun log(message: String)
	fun setCustomKey(key: String, value: Int)
	fun setCustomKey(key: String, value: Long)
	fun setCustomKey(key: String, value: Float)
	fun setCustomKey(key: String, value: Double)
	fun setCustomKey(key: String, value: String)
	fun setCustomKey(key: String, value: Boolean)
}
