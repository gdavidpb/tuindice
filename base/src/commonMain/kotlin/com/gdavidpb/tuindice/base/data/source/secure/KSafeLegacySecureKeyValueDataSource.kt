package com.gdavidpb.tuindice.base.data.source.secure

import com.gdavidpb.tuindice.base.data.repository.SecureKeyValueDataRepository
import eu.anifantakis.lib.ksafe.KSafe

class KSafeLegacySecureKeyValueDataSource(
	private val kSafe: KSafe
) : SecureKeyValueDataRepository {
	override suspend fun getString(key: String): String? {
		return kSafe.get<String?>(key = key, defaultValue = null)
	}

	override suspend fun putString(key: String, value: String) {
		kSafe.put(key = key, value = value)
	}

	override suspend fun remove(key: String) {
		kSafe.delete(key)
	}

	override suspend fun clear() {
		kSafe.clearAll()
	}
}
