package com.gdavidpb.tuindice.testkit.base.repository

import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import com.gdavidpb.tuindice.base.domain.model.SessionResetNotice
import com.gdavidpb.tuindice.base.domain.model.SessionResetNoticeReason
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository

class FakeSettingsRepository(
	private val reviewSuggested: Boolean = false,
	private var lastMainSection: MainSection = MainSection.SUMMARY,
	private var outdatedAppState: OutdatedAppState? = null,
	private val seenCoachmarkIds: MutableSet<String> = mutableSetOf(),
	private var legacyOnboardingCompleted: Boolean = false,
	private var localDataOwner: String? = null
) : SettingsRepository {
	var cleared = false
		private set

	override suspend fun isReviewSuggested(value: Int): Boolean = reviewSuggested

	override suspend fun getLastMainSection(): MainSection = lastMainSection

	override suspend fun setLastMainSection(section: MainSection) {
		lastMainSection = section
	}

	override suspend fun getOutdatedAppState(): OutdatedAppState? = outdatedAppState

	override suspend fun setOutdatedAppState(state: OutdatedAppState) {
		outdatedAppState = state
	}

	override suspend fun clearOutdatedAppState() {
		outdatedAppState = null
	}

	override suspend fun migrateLegacyOnboardingState(completedCoachmarkIds: Set<String>) {
		if (legacyOnboardingCompleted) {
			seenCoachmarkIds += completedCoachmarkIds
			legacyOnboardingCompleted = false
		}
	}

	override suspend fun getSeenCoachmarkIds(): Set<String> = seenCoachmarkIds.toSet()

	override suspend fun markCoachmarkSeen(coachmarkId: String) {
		seenCoachmarkIds += coachmarkId
	}

	override suspend fun setSessionResetNoticePending(reason: SessionResetNoticeReason): SessionResetNotice {
		val notice = SessionResetNotice(reason = reason, stamp = ++lastNoticeStamp)

		sessionResetNotice = notice

		return notice
	}

	override suspend fun getSessionResetNotice(): SessionResetNotice? = sessionResetNotice

	override suspend fun consumeSessionResetNotice(stamp: Long): Boolean {
		if (sessionResetNotice?.stamp != stamp) return false

		sessionResetNotice = null

		return true
	}

	override suspend fun getLocalDataOwner(): String? = localDataOwner

	override suspend fun setLocalDataOwner(usbId: String) {
		localDataOwner = usbId
	}

	override suspend fun clear() {
		cleared = true
		outdatedAppState = null
		seenCoachmarkIds.clear()
		legacyOnboardingCompleted = false
		localDataOwner = null
		sessionResetNotice = null
	}

	private var lastNoticeStamp = 0L

	var sessionResetNotice: SessionResetNotice? = null

	val sessionResetNoticeReason: SessionResetNoticeReason?
		get() = sessionResetNotice?.reason

	val sessionResetNoticePending: Boolean
		get() = sessionResetNotice != null
}
