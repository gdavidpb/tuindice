package com.gdavidpb.tuindice.data.source.credentials

import com.gdavidpb.tuindice.base.data.repository.SecureKeyValueDataRepository
import kotlin.test.Test
import kotlin.test.assertEquals
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

	private companion object {
		const val UNIVERSITY_PASSWORD_KEY = "universityPassword"
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
