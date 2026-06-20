package com.gdavidpb.tuindice.data.source.secure

import android.content.SharedPreferences
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AeadKeyTemplates
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class AndroidTinkSecureKeyValueDataSourceTest {
	@Test
	fun putAndGetEncryptsValueBeforePersistingIt() = runTest {
		val preferences = FakeSharedPreferences()
		val aeadProvider = TestAndroidSecureAeadProvider()
		val dataSource = AndroidTinkSecureKeyValueDataSource(
			valuePreferences = preferences,
			aeadProvider = aeadProvider
		)

		dataSource.putString(key = "sessionId", value = "session-1")

		assertEquals("session-1", dataSource.getString("sessionId"))
		assertNotEquals("session-1", preferences.values["sessionId"])
		assertFalse(preferences.values["sessionId"].isNullOrBlank())
	}

	@Test
	fun readFailsWhenCiphertextIsTampered() = runTest {
		val preferences = FakeSharedPreferences()
		val dataSource = AndroidTinkSecureKeyValueDataSource(
			valuePreferences = preferences,
			aeadProvider = TestAndroidSecureAeadProvider()
		)

		dataSource.putString(key = "accessToken", value = "token-1")
		preferences.values["accessToken"] = preferences.values.getValue("accessToken").tamperBase64()

		assertSecureReadFails {
			dataSource.getString("accessToken")
		}
	}

	@Test
	fun readFailsWhenCiphertextIsMovedToAnotherKeyBecauseAadChanges() = runTest {
		val preferences = FakeSharedPreferences()
		val dataSource = AndroidTinkSecureKeyValueDataSource(
			valuePreferences = preferences,
			aeadProvider = TestAndroidSecureAeadProvider()
		)

		dataSource.putString(key = "accessToken", value = "token-1")
		preferences.values["refreshToken"] = preferences.values.getValue("accessToken")

		assertSecureReadFails {
			dataSource.getString("refreshToken")
		}
	}

	@Test
	fun clearRemovesValuesAndClearsProvider() = runTest {
		val preferences = FakeSharedPreferences()
		val aeadProvider = TestAndroidSecureAeadProvider()
		val dataSource = AndroidTinkSecureKeyValueDataSource(
			valuePreferences = preferences,
			aeadProvider = aeadProvider
		)

		dataSource.putString(key = "refreshToken", value = "token-1")
		dataSource.clear()

		assertNull(dataSource.getString("refreshToken"))
		assertTrue(preferences.values.isEmpty())
		assertEquals(1, aeadProvider.clearCalls)
	}
}

private suspend fun assertSecureReadFails(block: suspend () -> Unit) {
	try {
		block()
		fail("Expected secure read to fail.")
	} catch (expected: IllegalStateException) {
		// Expected when Tink authentication fails.
	}
}

private fun String.tamperBase64(): String {
	val replacement = if (first() == 'A') 'B' else 'A'
	return replaceRange(0, 1, replacement.toString())
}

private class TestAndroidSecureAeadProvider : AndroidSecureAeadProvider {
	private val primitive = createAead()
	var clearCalls = 0
		private set

	override fun aead(): Aead = primitive

	override fun clear() {
		clearCalls++
	}

	private fun createAead(): Aead {
		AeadConfig.register()
		return KeysetHandle.generateNew(AeadKeyTemplates.AES256_GCM)
			.getPrimitive(RegistryConfiguration.get(), Aead::class.java)
	}
}

private class FakeSharedPreferences : SharedPreferences {
	val values = mutableMapOf<String, String>()

	override fun getString(key: String?, defValue: String?): String? {
		return values[key] ?: defValue
	}

	override fun edit(): SharedPreferences.Editor {
		return Editor()
	}

	override fun contains(key: String?): Boolean = values.containsKey(key)

	override fun getAll(): MutableMap<String, *> = values.toMutableMap()

	override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = defValues

	override fun getInt(key: String?, defValue: Int): Int = defValue

	override fun getLong(key: String?, defValue: Long): Long = defValue

	override fun getFloat(key: String?, defValue: Float): Float = defValue

	override fun getBoolean(key: String?, defValue: Boolean): Boolean = defValue

	override fun registerOnSharedPreferenceChangeListener(
		listener: SharedPreferences.OnSharedPreferenceChangeListener?
	) = Unit

	override fun unregisterOnSharedPreferenceChangeListener(
		listener: SharedPreferences.OnSharedPreferenceChangeListener?
	) = Unit

	private inner class Editor : SharedPreferences.Editor {
		private val updates = mutableMapOf<String, String?>()
		private var clearRequested = false

		override fun putString(key: String?, value: String?): SharedPreferences.Editor {
			requireNotNull(key)
			updates[key] = value
			return this
		}

		override fun remove(key: String?): SharedPreferences.Editor {
			requireNotNull(key)
			updates[key] = null
			return this
		}

		override fun clear(): SharedPreferences.Editor {
			clearRequested = true
			return this
		}

		override fun commit(): Boolean {
			if (clearRequested) values.clear()
			updates.forEach { (key, value) ->
				if (value == null) {
					values.remove(key)
				} else {
					values[key] = value
				}
			}
			return true
		}

		override fun apply() {
			commit()
		}

		override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = this

		override fun putInt(key: String?, value: Int): SharedPreferences.Editor = this

		override fun putLong(key: String?, value: Long): SharedPreferences.Editor = this

		override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = this

		override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = this
	}
}
