package com.gdavidpb.tuindice.data.source.settings

import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import com.gdavidpb.tuindice.base.domain.model.SessionResetNoticeReason
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.utils.PreferencesKeys
import com.gdavidpb.tuindice.data.mapper.toMainSectionOrThrow
import com.gdavidpb.tuindice.data.mapper.toPersistedName
import com.russhwolf.settings.Settings

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

	override suspend fun setSessionResetNoticePending(reason: SessionResetNoticeReason) {
		settings.putString(SESSION_RESET_NOTICE_REASON_KEY, reason.name)
	}

	override suspend fun getSessionResetNoticeReason(): SessionResetNoticeReason? {
		val stored = settings.getStringOrNull(SESSION_RESET_NOTICE_REASON_KEY)

		return SessionResetNoticeReason.entries.firstOrNull { reason -> reason.name == stored }
			// What the versions before the reason was kept left behind: a flag, which only a
			// start-up that could not read the data used to set.
			?: SessionResetNoticeReason.UnreadableData.takeIf {
				settings.getBooleanOrNull(LEGACY_SESSION_RESET_NOTICE_PENDING_KEY) == true
			}
	}

	override suspend fun consumeSessionResetNoticePending(): Boolean {
		val pending = getSessionResetNoticeReason() != null

		settings.remove(SESSION_RESET_NOTICE_REASON_KEY)
		settings.remove(LEGACY_SESSION_RESET_NOTICE_PENDING_KEY)

		return pending
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
private const val LOCAL_DATA_OWNER_KEY = "localDataOwner"
private const val OUTDATED_APP_MIN_VERSION_CODE_KEY = "outdatedAppMinVersionCode"
private const val LEGACY_WIZARD_COMPLETED_KEY = "wizardCompleted"
private const val LEGACY_GUIDED_TOUR_COMPLETED_KEY = "guidedTourCompleted"
private const val COACHMARK_LEGACY_MIGRATED_KEY = "contextualCoachmarkLegacyMigrated"
private const val COACHMARK_SEEN_IDS_KEY = "contextualCoachmarkSeenIds"
private const val COACHMARK_SEEN_IDS_SEPARATOR = ","
