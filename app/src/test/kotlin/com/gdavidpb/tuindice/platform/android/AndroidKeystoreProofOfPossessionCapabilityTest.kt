package com.gdavidpb.tuindice.platform.android

import com.gdavidpb.tuindice.base.data.repository.SecureKeyValueDataRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidKeystoreProofOfPossessionCapabilityTest {
	@Test
	fun resolveProofOfPossessionKeyIdMigratesLegacyKeyIdToActiveStore() = runTest {
		val activeStore = FakeSecureKeyValueDataRepository()
		val legacyStore = FakeSecureKeyValueDataRepository(
			initialValues = mapOf(ATTESTATION_KEY_ID to "legacy-key-id")
		)
		val capability = AndroidKeystoreProofOfPossessionCapability(
			secureStore = activeStore,
			legacySecureStore = legacyStore
		)

		assertEquals("legacy-key-id", capability.resolveProofOfPossessionKeyId())

		assertEquals("legacy-key-id", activeStore.values[ATTESTATION_KEY_ID])
		assertFalse(legacyStore.values.containsKey(ATTESTATION_KEY_ID))
	}

	@Test
	fun resolveProofOfPossessionKeyIdCreatesStableActiveKeyIdWhenStorageIsEmpty() = runTest {
		val activeStore = FakeSecureKeyValueDataRepository()
		val legacyStore = FakeSecureKeyValueDataRepository()
		val capability = AndroidKeystoreProofOfPossessionCapability(
			secureStore = activeStore,
			legacySecureStore = legacyStore
		)

		val keyId = capability.resolveProofOfPossessionKeyId()

		assertTrue(keyId.isNotBlank())
		assertEquals(keyId, capability.resolveProofOfPossessionKeyId())
		assertEquals(keyId, activeStore.values[ATTESTATION_KEY_ID])
	}

	private companion object {
		const val ATTESTATION_KEY_ID = "attestationProofOfPossessionKeyId"
	}
}

private class FakeSecureKeyValueDataRepository(
	initialValues: Map<String, String> = emptyMap()
) : SecureKeyValueDataRepository {
	val values = initialValues.toMutableMap()

	override suspend fun getString(key: String): String? = values[key]

	override suspend fun putString(key: String, value: String) {
		values[key] = value
	}

	override suspend fun remove(key: String) {
		values.remove(key)
	}

	override suspend fun clear() {
		values.clear()
	}
}
