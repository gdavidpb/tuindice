package com.gdavidpb.tuindice.base.data.source.usage

import com.russhwolf.settings.Settings
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UsageDataConsentSettingsDataSourceTest {
	@Test
	fun isUsageDataCollectionEnabled_defaultsToFalse() {
		val dataSource = UsageDataConsentSettingsDataSource(FakeSettings())

		assertFalse(dataSource.isUsageDataCollectionEnabled())
	}

	@Test
	fun isUsageDataCollectionEnabled_readsLegacyAnalyticsKeyAsFallback() {
		val settings = FakeSettings().apply {
			putBoolean("analyticsCollectionEnabled", true)
		}
		val dataSource = UsageDataConsentSettingsDataSource(settings)

		assertTrue(dataSource.isUsageDataCollectionEnabled())
	}

	@Test
	fun setUsageDataCollectionEnabled_writesNewUsageDataKey() {
		val settings = FakeSettings().apply {
			putBoolean("analyticsCollectionEnabled", true)
		}
		val dataSource = UsageDataConsentSettingsDataSource(settings)

		dataSource.setUsageDataCollectionEnabled(false)

		assertFalse(dataSource.isUsageDataCollectionEnabled())
		assertFalse(settings.getBoolean("usageDataCollectionEnabled", true))
		assertTrue(settings.getBoolean("analyticsCollectionEnabled", false))
	}
}

private class FakeSettings : Settings {
	private val values = mutableMapOf<String, Any>()

	override val keys: Set<String> get() = values.keys
	override val size: Int get() = values.size

	override fun clear() {
		values.clear()
	}

	override fun remove(key: String) {
		values.remove(key)
	}

	override fun hasKey(key: String): Boolean = key in values

	override fun putInt(key: String, value: Int) {
		values[key] = value
	}

	override fun getInt(key: String, defaultValue: Int): Int = values[key] as? Int ?: defaultValue

	override fun getIntOrNull(key: String): Int? = values[key] as? Int

	override fun putLong(key: String, value: Long) {
		values[key] = value
	}

	override fun getLong(key: String, defaultValue: Long): Long = values[key] as? Long ?: defaultValue

	override fun getLongOrNull(key: String): Long? = values[key] as? Long

	override fun putString(key: String, value: String) {
		values[key] = value
	}

	override fun getString(key: String, defaultValue: String): String = values[key] as? String ?: defaultValue

	override fun getStringOrNull(key: String): String? = values[key] as? String

	override fun putFloat(key: String, value: Float) {
		values[key] = value
	}

	override fun getFloat(key: String, defaultValue: Float): Float = values[key] as? Float ?: defaultValue

	override fun getFloatOrNull(key: String): Float? = values[key] as? Float

	override fun putDouble(key: String, value: Double) {
		values[key] = value
	}

	override fun getDouble(key: String, defaultValue: Double): Double = values[key] as? Double ?: defaultValue

	override fun getDoubleOrNull(key: String): Double? = values[key] as? Double

	override fun putBoolean(key: String, value: Boolean) {
		values[key] = value
	}

	override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
		values[key] as? Boolean ?: defaultValue

	override fun getBooleanOrNull(key: String): Boolean? = values[key] as? Boolean
}
