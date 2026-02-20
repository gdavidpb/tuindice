package com.gdavidpb.tuindice.record.data.repository.quarter.source

import android.content.SharedPreferences
import com.gdavidpb.tuindice.record.utils.CooldownTimes
import com.gdavidpb.tuindice.record.utils.PreferencesKeys
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PreferencesDataSourceTest {
    @Test
    fun isGetQuartersOnCooldown_whenStoredTimestampIsFuture_returnsTrue() = runBlocking {
        val sharedPreferences = FakeSharedPreferences()
        val dataSource = PreferencesDataSource(sharedPreferences)
        sharedPreferences.edit()
            .putLong(
                PreferencesKeys.COOLDOWN_GET_QUARTERS,
                System.currentTimeMillis() + 10_000L
            )
            .apply()

        val result = dataSource.isGetQuartersOnCooldown()

        assertTrue(result)
    }

    @Test
    fun isGetQuartersOnCooldown_whenStoredTimestampIsPast_returnsFalse() = runBlocking {
        val sharedPreferences = FakeSharedPreferences()
        val dataSource = PreferencesDataSource(sharedPreferences)
        sharedPreferences.edit()
            .putLong(
                PreferencesKeys.COOLDOWN_GET_QUARTERS,
                System.currentTimeMillis() - 10_000L
            )
            .apply()

        val result = dataSource.isGetQuartersOnCooldown()

        assertFalse(result)
    }

    @Test
    fun setGetQuartersOnCooldown_storesFutureTimestamp() = runBlocking {
        val sharedPreferences = FakeSharedPreferences()
        val dataSource = PreferencesDataSource(sharedPreferences)

        val before = System.currentTimeMillis()
        dataSource.setGetQuartersOnCooldown()
        val after = System.currentTimeMillis()

        val storedValue = sharedPreferences.getLong(
            PreferencesKeys.COOLDOWN_GET_QUARTERS,
            0L
        )

        assertTrue(storedValue >= before + CooldownTimes.COOLDOWN_GET_QUARTERS)
        assertTrue(storedValue <= after + CooldownTimes.COOLDOWN_GET_QUARTERS + 250L)
    }
}

private class FakeSharedPreferences : SharedPreferences {
    private val storage = mutableMapOf<String, Any?>()

    override fun getAll(): MutableMap<String, *> = storage.toMutableMap()

    override fun getString(key: String?, defValue: String?): String? {
        return storage[key] as? String ?: defValue
    }

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
        @Suppress("UNCHECKED_CAST")
        return (storage[key] as? MutableSet<String>) ?: defValues
    }

    override fun getInt(key: String?, defValue: Int): Int {
        return storage[key] as? Int ?: defValue
    }

    override fun getLong(key: String?, defValue: Long): Long {
        return storage[key] as? Long ?: defValue
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        return storage[key] as? Float ?: defValue
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return storage[key] as? Boolean ?: defValue
    }

    override fun contains(key: String?): Boolean {
        return storage.containsKey(key)
    }

    override fun edit(): SharedPreferences.Editor = Editor(storage)

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        // No-op for tests.
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        // No-op for tests.
    }

    private class Editor(
        private val storage: MutableMap<String, Any?>
    ) : SharedPreferences.Editor {
        private val pending = mutableMapOf<String, Any?>()
        private var clearRequested = false

        override fun putString(key: String?, value: String?): SharedPreferences.Editor = apply {
            pending[key.orEmpty()] = value
        }

        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = apply {
            pending[key.orEmpty()] = values
        }

        override fun putInt(key: String?, value: Int): SharedPreferences.Editor = apply {
            pending[key.orEmpty()] = value
        }

        override fun putLong(key: String?, value: Long): SharedPreferences.Editor = apply {
            pending[key.orEmpty()] = value
        }

        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = apply {
            pending[key.orEmpty()] = value
        }

        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = apply {
            pending[key.orEmpty()] = value
        }

        override fun remove(key: String?): SharedPreferences.Editor = apply {
            pending[key.orEmpty()] = null
        }

        override fun clear(): SharedPreferences.Editor = apply {
            clearRequested = true
            pending.clear()
        }

        override fun commit(): Boolean {
            apply()
            return true
        }

        override fun apply() {
            if (clearRequested) storage.clear()

            pending.forEach { (key, value) ->
                if (value == null) storage.remove(key)
                else storage[key] = value
            }
        }
    }
}