package com.gdavidpb.tuindice.data.source.settings

import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import com.gdavidpb.tuindice.base.domain.model.SessionResetNotice
import com.gdavidpb.tuindice.base.domain.model.SessionResetNoticeReason
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.utils.PreferencesKeys
import com.gdavidpb.tuindice.data.mapper.toMainSectionOrThrow
import com.gdavidpb.tuindice.data.mapper.toPersistedName
import com.russhwolf.settings.Settings
import kotlin.random.Random

class MultiplatformSettingsDataSource(
	private val settings: Settings
) : SettingsRepository {
	override suspend fun getLastMainSection(): MainSection {
		return settings.getStringOrNull(LAST_MAIN_SECTION_KEY)
			?.toMainSectionOrThrow()
			?: MainSection.SUMMARY
	}

	override suspend fun setLastMainSection(section: MainSection) {
		settings.putString(
			key = LAST_MAIN_SECTION_KEY,
			value = section.toPersistedName()
		)
	}

	override suspend fun getOutdatedAppState(): OutdatedAppState? {
		return settings.getLongOrNull(OUTDATED_APP_MIN_VERSION_CODE_KEY)
			?.let { minimumVersionCode -> OutdatedAppState(minimumVersionCode = minimumVersionCode) }
	}

	override suspend fun setOutdatedAppState(state: OutdatedAppState) {
		settings.putLong(OUTDATED_APP_MIN_VERSION_CODE_KEY, state.minimumVersionCode)
	}

	override suspend fun clearOutdatedAppState() {
		settings.remove(OUTDATED_APP_MIN_VERSION_CODE_KEY)
	}

	override suspend fun isReviewSuggested(value: Int): Boolean {
		val counter = (settings.getIntOrNull(PreferencesKeys.SYNCS_COUNTER) ?: 0) + 1

		settings.putInt(PreferencesKeys.SYNCS_COUNTER, counter)

		return counter == value
	}

	override suspend fun migrateLegacyOnboardingState(completedCoachmarkIds: Set<String>) {
		if (settings.getBooleanOrNull(COACHMARK_LEGACY_MIGRATED_KEY) == true) return

		val completedLegacyFlow = settings.getBooleanOrNull(LEGACY_WIZARD_COMPLETED_KEY) == true ||
			settings.getBooleanOrNull(LEGACY_GUIDED_TOUR_COMPLETED_KEY) == true

		if (completedLegacyFlow) {
			val seenCoachmarkIds = getSeenCoachmarkIds() + completedCoachmarkIds
			settings.putString(
				key = COACHMARK_SEEN_IDS_KEY,
				value = seenCoachmarkIds.sorted().joinToString(COACHMARK_SEEN_IDS_SEPARATOR)
			)
		}

		settings.putBoolean(COACHMARK_LEGACY_MIGRATED_KEY, true)
	}

	override suspend fun getSeenCoachmarkIds(): Set<String> {
		return settings.getStringOrNull(COACHMARK_SEEN_IDS_KEY)
			.orEmpty()
			.split(COACHMARK_SEEN_IDS_SEPARATOR)
			.mapNotNull { coachmarkId -> coachmarkId.trim().takeIf(String::isNotBlank) }
			.toSet()
	}

	override suspend fun markCoachmarkSeen(coachmarkId: String) {
		val seenCoachmarkIds = getSeenCoachmarkIds() + coachmarkId
		settings.putString(
			key = COACHMARK_SEEN_IDS_KEY,
			value = seenCoachmarkIds.sorted().joinToString(COACHMARK_SEEN_IDS_SEPARATOR)
		)
	}

	// Reason and stamp are one value, so a reader never sees one without the other.
	override suspend fun setSessionResetNoticePending(reason: SessionResetNoticeReason): SessionResetNotice {
		val notice = SessionResetNotice(reason = reason, stamp = Random.nextLong(from = 1, until = Long.MAX_VALUE))

		settings.putString(SESSION_RESET_NOTICE_REASON_KEY, "${notice.reason.name}$STAMP_SEPARATOR${notice.stamp}")

		return notice
	}

	override suspend fun getSessionResetNotice(): SessionResetNotice? {
		val stored = settings.getStringOrNull(SESSION_RESET_NOTICE_REASON_KEY)
		val reason = SessionResetNoticeReason.entries.firstOrNull { reason ->
			reason.name == stored?.substringBefore(STAMP_SEPARATOR)
		}

		if (reason != null) {
			// A reason kept by the versions before the stamp has none: it takes the fixed one.
			val stamp = stored?.substringAfter(STAMP_SEPARATOR, missingDelimiterValue = "")?.toLongOrNull()

			return SessionResetNotice(reason = reason, stamp = stamp ?: LEGACY_NOTICE_STAMP)
		}

		// What the versions before the reason was kept left behind: a flag, which only a
		// start-up that could not read the data used to set.
		return SessionResetNotice(
			reason = SessionResetNoticeReason.UnreadableData,
			stamp = LEGACY_NOTICE_STAMP
		).takeIf { settings.getBooleanOrNull(LEGACY_SESSION_RESET_NOTICE_PENDING_KEY) == true }
	}

	override suspend fun consumeSessionResetNotice(stamp: Long): Boolean {
		if (getSessionResetNotice()?.stamp != stamp) return false

		settings.remove(SESSION_RESET_NOTICE_REASON_KEY)
		settings.remove(LEGACY_SESSION_RESET_NOTICE_PENDING_KEY)

		return true
	}

	override suspend fun getLocalDataOwner(): String? {
		return settings.getStringOrNull(LOCAL_DATA_OWNER_KEY)
	}

	override suspend fun setLocalDataOwner(usbId: String) {
		settings.putString(LOCAL_DATA_OWNER_KEY, usbId)
	}

	override suspend fun clear() {
		settings.clear()
	}
}

private const val LAST_MAIN_SECTION_KEY = "lastDestination"
private const val SESSION_RESET_NOTICE_REASON_KEY = "sessionResetNoticeReason"
private const val LEGACY_SESSION_RESET_NOTICE_PENDING_KEY = "sessionResetNoticePending"
private const val STAMP_SEPARATOR = ":"
private const val LEGACY_NOTICE_STAMP = 0L
private const val LOCAL_DATA_OWNER_KEY = "localDataOwner"
private const val OUTDATED_APP_MIN_VERSION_CODE_KEY = "outdatedAppMinVersionCode"
private const val LEGACY_WIZARD_COMPLETED_KEY = "wizardCompleted"
private const val LEGACY_GUIDED_TOUR_COMPLETED_KEY = "guidedTourCompleted"
private const val COACHMARK_LEGACY_MIGRATED_KEY = "contextualCoachmarkLegacyMigrated"
private const val COACHMARK_SEEN_IDS_KEY = "contextualCoachmarkSeenIds"
private const val COACHMARK_SEEN_IDS_SEPARATOR = ","
