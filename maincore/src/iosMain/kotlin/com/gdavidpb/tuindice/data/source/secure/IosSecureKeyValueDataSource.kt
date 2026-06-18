package com.gdavidpb.tuindice.data.source.secure

import com.gdavidpb.tuindice.base.data.repository.SecureKeyValueDataRepository
import com.gdavidpb.tuindice.platform.IosSecureStoreCapability

class IosSecureKeyValueDataSource(
	private val capability: IosSecureStoreCapability
) : SecureKeyValueDataRepository {
	override suspend fun getString(key: String): String? {
		return capability.readSecureValue(key)
	}

	override suspend fun putString(key: String, value: String) {
		capability.writeSecureValue(key = key, value = value)
	}

	override suspend fun remove(key: String) {
		capability.deleteSecureValue(key)
	}

	override suspend fun clear() {
		capability.clearSecureValues()
	}
}
