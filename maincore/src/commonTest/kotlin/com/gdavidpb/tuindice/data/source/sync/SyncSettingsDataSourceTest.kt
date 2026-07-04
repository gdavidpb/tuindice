package com.gdavidpb.tuindice.data.source.sync

import com.gdavidpb.tuindice.data.model.SyncRetryBackoffState
import com.russhwolf.settings.Settings
import kotlinx.coroutines.test.runTest
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import com.gdavidpb.tuindice.evaluations.utils.PreferencesKeys as EvaluationsPreferencesKeys
import com.gdavidpb.tuindice.record.utils.PreferencesKeys as RecordPreferencesKeys
import com.gdavidpb.tuindice.summary.utils.PreferencesKeys as SummaryPreferencesKeys

class SyncSettingsDataSourceTest {
	@Test
	fun setSyncedFeatureCooldowns_marksSummaryAndRecordCooldowns() = runTest {
		val settings = FakeSettings()
		val dataSource = SyncSettingsDataSource(settings)

		dataSource.setSyncedFeatureCooldowns()

		assertTrue(settings.hasKey(SummaryPreferencesKeys.COOLDOWN_GET_USER))
		assertTrue(settings.hasKey(RecordPreferencesKeys.COOLDOWN_GET_RECORD))
		assertFalse(settings.hasKey(EvaluationsPreferencesKeys.COOLDOWN_GET_EVALUATIONS))
	}

	@Test
	fun clearRecoveryCooldowns_removesGlobalAndFeatureCooldowns() = runTest {
		val settings = FakeSettings()
		val dataSource = SyncSettingsDataSource(settings)
		dataSource.setSyncOnCooldown()
		dataSource.setSyncedFeatureCooldowns()
		settings.putLong(EvaluationsPreferencesKeys.COOLDOWN_GET_EVALUATIONS, Long.MAX_VALUE)

		dataSource.clearRecoveryCooldowns()

		assertFalse(dataSource.isSyncOnCooldown())
		assertFalse(settings.hasKey(SummaryPreferencesKeys.COOLDOWN_GET_USER))
		assertFalse(settings.hasKey(RecordPreferencesKeys.COOLDOWN_GET_RECORD))
		assertFalse(settings.hasKey(EvaluationsPreferencesKeys.COOLDOWN_GET_EVALUATIONS))
	}

	@Test
	fun markSyncRetryBackoff_usesOneHourBase() = runTest {
		val settings = FakeSettings()
		val dataSource = SyncSettingsDataSource(settings)

		dataSource.markSyncRetryBackoff(RuntimeException("network"))
		val state = dataSource.getSyncRetryBackoffState()

		assertEquals(1, state.retryCount)
		assertTrue(dataSource.isSyncRetryBackoffActive())
		assertDelayInMillisIsCloseTo(
			expected = 1.hours.inWholeMilliseconds,
			actual = state.retryBackoffUntil - state.lastRetryAt
		)
	}

	@Test
	fun markSyncRetryBackoff_usesExponentialBackoffAndCapsAt24Hours() = runTest {
		val settings = FakeSettings()
		val dataSource = SyncSettingsDataSource(settings)
		val expectedDelays = listOf(
			1L,
			2L,
			4L,
			8L,
			16L,
			24L,
			24L
		).map { it.hours.inWholeMilliseconds }

		expectedDelays.forEachIndexed { index, expectedDelay ->
			dataSource.markSyncRetryBackoff(RuntimeException("attempt-${index + 1}"))
			val state = dataSource.getSyncRetryBackoffState()

			assertEquals(index + 1, state.retryCount)
			assertDelayInMillisIsCloseTo(
				expected = expectedDelay,
				actual = state.retryBackoffUntil - state.lastRetryAt
			)
		}
	}

	@Test
	fun clearSyncRetryBackoff_resetsRetryState() = runTest {
		val settings = FakeSettings()
		val dataSource = SyncSettingsDataSource(settings)

		dataSource.markSyncRetryBackoff(RuntimeException("network"))
		dataSource.clearSyncRetryBackoff()

		assertFalse(dataSource.isSyncRetryBackoffActive())
		assertEquals(
			SyncRetryBackoffState(
				retryCount = 0,
				lastRetryAt = 0L,
				retryBackoffUntil = 0L
			),
			dataSource.getSyncRetryBackoffState()
		)
	}

	private fun assertDelayInMillisIsCloseTo(
		expected: Long,
		actual: Long,
		tolerance: Long = 250L
	) {
		assertTrue(abs(actual - expected) <= tolerance)
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

	override fun getBoolean(key: String, defaultValue: Boolean): Boolean = values[key] as? Boolean ?: defaultValue

	override fun getBooleanOrNull(key: String): Boolean? = values[key] as? Boolean
}
