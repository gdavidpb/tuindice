package com.gdavidpb.tuindice.base.domain.session

import com.gdavidpb.tuindice.base.domain.model.SessionResetNoticeReason
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.utils.extension.attempt

/**
 * Announces that a session ended. The notice is kept in the settings before the live notice, so that an
 * interface that never collects it (a process woken without UI that dies first) still explains the sign-in
 * screen at the next start. The interface that does show it live takes it by its stamp, and an intentional
 * sign-out, whose live notice is suppressed, takes it back here: nobody is owed an explanation of a sign-out.
 * Whoever calls this has already emptied the stores, which is what the notice explains.
 *
 * `SecureStoreSessionDataSource.invalidateStoredSession` does not use this helper on purpose. It keeps the
 * notice before it empties the stores, because emptying the legacy store really suspends: a reader cancelled
 * there would otherwise leave both stores empty with nothing to explain it at the next start (see the comment
 * in that function). The order of its steps is therefore different from the one here.
 */
suspend fun SessionInvalidationRepository.announceInvalidation(
	settingsRepository: SettingsRepository,
	reason: SessionResetNoticeReason,
	sessionId: String?
) {
	val notice = attempt { settingsRepository.setSessionResetNoticePending(reason) }.getOrNull()

	val raised = attempt { notifySessionInvalidated(sessionId = sessionId) }.getOrDefault(false)

	if (!raised && notice != null) attempt { settingsRepository.consumeSessionResetNotice(notice.stamp) }
}
