package com.gdavidpb.tuindice.platform

interface IosSecureStoreCapability {
	fun readSecureValue(key: String): String?
	fun writeSecureValue(key: String, value: String)
	fun deleteSecureValue(key: String)
	fun clearSecureValues()
}
