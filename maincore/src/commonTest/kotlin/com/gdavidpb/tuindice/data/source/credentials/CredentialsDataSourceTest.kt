package com.gdavidpb.tuindice.data.source.credentials

import com.gdavidpb.tuindice.base.data.repository.SecureKeyValueDataRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class CredentialsDataSourceTest {
	@Test
	fun readsPasswordFromActiveSecureStore() = runTest {
		val activeStore = FakeSecureKeyValueDataRepository(
			initialValues = mapOf(UNIVERSITY_PASSWORD_KEY to "active-password")
		)
		val legacyStore = FakeSecureKeyValueDataRepository(
			initialValues = mapOf(UNIVERSITY_PASSWORD_KEY to "legacy-password")
		)
		val dataSource = CredentialsDataSource(
			secureStore = activeStore,
			legacySecureStore = legacyStore
		)

		assertTrue(dataSource.hasPassword())
		assertEquals("active-password", dataSource.getPassword())
		assertEquals("legacy-password", legacyStore.values[UNIVERSITY_PASSWORD_KEY])
	}

	@Test
	fun migratesPasswordFromLegacyStoreAndDeletesLegacyValue() = runTest {
		val activeStore = FakeSecureKeyValueDataRepository()
		val legacyStore = FakeSecureKeyValueDataRepository(
			initialValues = mapOf(UNIVERSITY_PASSWORD_KEY to "legacy-password")
		)
		val dataSource = CredentialsDataSource(
			secureStore = activeStore,
			legacySecureStore = legacyStore
		)

		assertEquals("legacy-password", dataSource.getPassword())

		assertEquals("legacy-password", activeStore.values[UNIVERSITY_PASSWORD_KEY])
		assertNull(legacyStore.values[UNIVERSITY_PASSWORD_KEY])
	}

	@Test
	fun setPasswordWritesOnlyTheActiveStore() = runTest {
		val activeStore = FakeSecureKeyValueDataRepository()
		val legacyStore = FakeSecureKeyValueDataRepository(
			initialValues = mapOf(UNIVERSITY_PASSWORD_KEY to "legacy-password")
		)
		val dataSource = CredentialsDataSource(
			secureStore = activeStore,
			legacySecureStore = legacyStore
		)

		dataSource.setPassword("new-password")

		assertEquals("new-password", activeStore.values[UNIVERSITY_PASSWORD_KEY])
		assertNull(legacyStore.values[UNIVERSITY_PASSWORD_KEY])
	}

	@Test
	fun setPasswordKeepsPasswordAvailableWhenActiveStoreReadReturnsNull() = runTest {
		val activeStore = FakeSecureKeyValueDataRepository(dropReads = true)
		val legacyStore = FakeSecureKeyValueDataRepository()
		val dataSource = CredentialsDataSource(
			secureStore = activeStore,
			legacySecureStore = legacyStore
		)

		dataSource.setPassword("new-password")

		assertTrue(dataSource.hasPassword())
		assertEquals("new-password", dataSource.getPassword())
		assertEquals("new-password", activeStore.values[UNIVERSITY_PASSWORD_KEY])
	}

	// A store that cannot be read right now is not a store without a password: answering false
	// would let ScheduleSyncUseCase latch MissingCredentials over a perfectly good password.
	@Test
	fun hasPassword_whenTheActiveStoreFailsToRead_throwsInsteadOfAnsweringFalse() = runTest {
		val dataSource = CredentialsDataSource(
			secureStore = FakeSecureKeyValueDataRepository(failReads = true),
			legacySecureStore = FakeSecureKeyValueDataRepository()
		)

		assertFailsWith<IllegalStateException> { dataSource.hasPassword() }
	}

	@Test
	fun clearPasswordRemovesActiveAndLegacyValues() = runTest {
		val activeStore = FakeSecureKeyValueDataRepository(
			initialValues = mapOf(UNIVERSITY_PASSWORD_KEY to "active-password")
		)
		val legacyStore = FakeSecureKeyValueDataRepository(
			initialValues = mapOf(UNIVERSITY_PASSWORD_KEY to "legacy-password")
		)
		val dataSource = CredentialsDataSource(
			secureStore = activeStore,
			legacySecureStore = legacyStore
		)

		dataSource.clearPassword()

		assertFalse(dataSource.hasPassword())
		assertNull(activeStore.values[UNIVERSITY_PASSWORD_KEY])
		assertNull(legacyStore.values[UNIVERSITY_PASSWORD_KEY])
	}

	@Test
	fun clearPasswordClearsMemoryPassword() = runTest {
		val activeStore = FakeSecureKeyValueDataRepository(dropReads = true)
		val legacyStore = FakeSecureKeyValueDataRepository()
		val dataSource = CredentialsDataSource(
			secureStore = activeStore,
			legacySecureStore = legacyStore
		)

		dataSource.setPassword("new-password")
		dataSource.clearPassword()

		assertFalse(dataSource.hasPassword())
	}

	// Signing out wipes the stores without going through clearPassword: the copy held in memory
	// must not outlive them.
	@Test
	fun clearSessionMemory_afterTheStoresAreWiped_dropsThePasswordHeldInMemory() = runTest {
		val activeStore = FakeSecureKeyValueDataRepository()
		val legacyStore = FakeSecureKeyValueDataRepository()
		val dataSource = CredentialsDataSource(
			secureStore = activeStore,
			legacySecureStore = legacyStore
		)

		dataSource.setPassword("signed-out-password")
		activeStore.clear()
		legacyStore.clear()
		dataSource.clearSessionMemory()

		assertFalse(dataSource.hasPassword())
	}

	@Test
	fun clearSessionMemory_whenTheStoreStillHoldsThePassword_readsItFromTheStore() = runTest {
		val activeStore = FakeSecureKeyValueDataRepository()
		val dataSource = CredentialsDataSource(
			secureStore = activeStore,
			legacySecureStore = FakeSecureKeyValueDataRepository()
		)

		dataSource.setPassword("stored-password")
		dataSource.clearSessionMemory()

		assertEquals("stored-password", dataSource.getPassword())
	}

	private companion object {
		const val UNIVERSITY_PASSWORD_KEY = "universityPassword"
	}
}

private class FakeSecureKeyValueDataRepository(
	initialValues: Map<String, String> = emptyMap(),
	private val dropReads: Boolean = false,
	private val failReads: Boolean = false
) : SecureKeyValueDataRepository {
	val values = initialValues.toMutableMap()

	override suspend fun getString(key: String): String? {
		if (failReads) error("secure store unavailable")

		return if (dropReads) null else values[key]
	}

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
