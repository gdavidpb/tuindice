package com.gdavidpb.tuindice.data.source.sync

import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.data.repository.sync.SyncSettingsLocalDataRepository
import com.russhwolf.settings.Settings
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import com.gdavidpb.tuindice.evaluations.utils.PreferencesKeys as EvaluationsPreferencesKeys
import com.gdavidpb.tuindice.record.utils.CooldownTimes as RecordCooldownTimes
import com.gdavidpb.tuindice.record.utils.PreferencesKeys as RecordPreferencesKeys
import com.gdavidpb.tuindice.summary.utils.CooldownTimes as SummaryCooldownTimes
import com.gdavidpb.tuindice.summary.utils.PreferencesKeys as SummaryPreferencesKeys
import com.gdavidpb.tuindice.data.model.SyncRetryBackoffState

class SyncSettingsDataSource(
	private val settings: Settings
) : SyncSettingsLocalDataRepository {
	override suspend fun isSyncOnCooldown(): Boolean {
		val cooldownTime = settings.getLongOrNull(PreferencesKeys.COOLDOWN_SYNC) ?: 0L

		return cooldownTime >= currentTimeMillis()
	}

	override suspend fun isSyncRetryBackoffActive(): Boolean {
		val retryBackoffUntil = settings.getLongOrNull(PreferencesKeys.SYNC_RETRY_UNTIL) ?: 0L

		return retryBackoffUntil >= currentTimeMillis()
	}

	override suspend fun setSyncOnCooldown() {
		val cooldownTime = currentTimeMillis() + CooldownTimes.COOLDOWN_SYNC

		settings.putLong(PreferencesKeys.COOLDOWN_SYNC, cooldownTime)
	}

	override suspend fun setSyncedFeatureCooldowns() {
		val now = currentTimeMillis()

		settings.putLong(
			SummaryPreferencesKeys.COOLDOWN_GET_USER,
			now + SummaryCooldownTimes.COOLDOWN_GET_USER
		)
		settings.putLong(
			RecordPreferencesKeys.COOLDOWN_GET_RECORD,
			now + RecordCooldownTimes.COOLDOWN_GET_RECORD
		)
	}

	override suspend fun clearStaleFeatureCooldowns() {
		settings.remove(EvaluationsPreferencesKeys.COOLDOWN_GET_EVALUATIONS)
	}

	override suspend fun clearRecoveryCooldowns() {
		settings.remove(PreferencesKeys.COOLDOWN_SYNC)
		settings.remove(SummaryPreferencesKeys.COOLDOWN_GET_USER)
		settings.remove(RecordPreferencesKeys.COOLDOWN_GET_RECORD)
		settings.remove(EvaluationsPreferencesKeys.COOLDOWN_GET_EVALUATIONS)
	}

	override suspend fun markSyncRetryBackoff(throwable: Throwable) {
		val now = currentTimeMillis()
		val nextRetryCount = (settings.getIntOrNull(PreferencesKeys.SYNC_RETRY_COUNT) ?: 0) + 1
		val retryBackoff = calculateSyncRetryBackoff(retryCount = nextRetryCount)

		settings.putInt(PreferencesKeys.SYNC_RETRY_COUNT, nextRetryCount)
		settings.putLong(PreferencesKeys.SYNC_RETRY_LAST_AT, now)
		settings.putLong(PreferencesKeys.SYNC_RETRY_UNTIL, now + retryBackoff)
	}

	override suspend fun clearSyncRetryBackoff() {
		settings.remove(PreferencesKeys.SYNC_RETRY_COUNT)
		settings.remove(PreferencesKeys.SYNC_RETRY_LAST_AT)
		settings.remove(PreferencesKeys.SYNC_RETRY_UNTIL)
	}

	override suspend fun getSyncRetryBackoffState(): SyncRetryBackoffState {
		return SyncRetryBackoffState(
			retryCount = settings.getIntOrNull(PreferencesKeys.SYNC_RETRY_COUNT) ?: 0,
			lastRetryAt = settings.getLongOrNull(PreferencesKeys.SYNC_RETRY_LAST_AT) ?: 0L,
			retryBackoffUntil = settings.getLongOrNull(PreferencesKeys.SYNC_RETRY_UNTIL) ?: 0L
		)
	}

	private fun calculateSyncRetryBackoff(retryCount: Int): Long {
		var delayInMs = CooldownTimes.BACKOFF_SYNC
		repeat((retryCount - 1).coerceIn(0, 5)) {
			delayInMs = (delayInMs * 2).coerceAtMost(CooldownTimes.BACKOFF_SYNC_MAX)
		}

		return delayInMs
	}

	private object PreferencesKeys {
		const val COOLDOWN_SYNC = "cooldownSync"
		const val SYNC_RETRY_COUNT = "syncRetryCount"
		const val SYNC_RETRY_UNTIL = "syncRetryUntil"
		const val SYNC_RETRY_LAST_AT = "syncRetryLastAt"
	}

	private object CooldownTimes {
		val COOLDOWN_SYNC = 1.days.inWholeMilliseconds
		val BACKOFF_SYNC = 1.hours.inWholeMilliseconds
		val BACKOFF_SYNC_MAX = 24.hours.inWholeMilliseconds
	}
}
