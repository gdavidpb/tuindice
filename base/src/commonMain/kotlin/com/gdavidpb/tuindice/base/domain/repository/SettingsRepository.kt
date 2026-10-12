package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
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

	suspend fun setSessionResetNoticePending(reason: SessionResetNoticeReason)

	/** Why the notice that is still owed to the user was left, or null when none is pending. */
	suspend fun getSessionResetNoticeReason(): SessionResetNoticeReason?
	suspend fun consumeSessionResetNoticePending(): Boolean

	suspend fun getLocalDataOwner(): String?
	suspend fun setLocalDataOwner(usbId: String)

	suspend fun clear()
}
