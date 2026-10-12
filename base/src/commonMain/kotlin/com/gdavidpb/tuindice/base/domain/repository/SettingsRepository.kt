package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import com.gdavidpb.tuindice.base.domain.model.SessionResetNotice
import com.gdavidpb.tuindice.base.domain.model.SessionResetNoticeReason

interface SettingsRepository {
	suspend fun isReviewSuggested(value: Int): Boolean

	suspend fun getLastMainSection(): MainSection
	suspend fun setLastMainSection(section: MainSection)

	suspend fun getOutdatedAppState(): OutdatedAppState?
	suspend fun setOutdatedAppState(state: OutdatedAppState)
	suspend fun clearOutdatedAppState()

	suspend fun migrateLegacyOnboardingState(completedCoachmarkIds: Set<String>)
	suspend fun getSeenCoachmarkIds(): Set<String>
	suspend fun markCoachmarkSeen(coachmarkId: String)

	/** Keeps a notice owed to the user and returns it, with the stamp that identifies it. */
	suspend fun setSessionResetNoticePending(reason: SessionResetNoticeReason): SessionResetNotice

	/** The notice that is still owed to the user, or null when none is pending. */
	suspend fun getSessionResetNotice(): SessionResetNotice?

	/** Takes the pending notice only if it is the one with this [stamp]; true when it was taken. */
	suspend fun consumeSessionResetNotice(stamp: Long): Boolean

	suspend fun getLocalDataOwner(): String?
	suspend fun setLocalDataOwner(usbId: String)

	suspend fun clear()
}
