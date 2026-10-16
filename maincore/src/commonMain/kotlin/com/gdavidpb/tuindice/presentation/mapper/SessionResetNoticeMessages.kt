package com.gdavidpb.tuindice.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.SessionResetNoticeReason
import org.jetbrains.compose.resources.getString
import tuindice.maincore.generated.resources.Res
import tuindice.maincore.generated.resources.snack_session_invalidated
import tuindice.maincore.generated.resources.snack_session_reset_on_startup

// What the user is told about a session that was reset behind their back: the text that says what
// happened, whichever way the notice reaches them (live, or at the next start after the process died).
internal suspend fun SessionResetNoticeReason.toNoticeText(): String {
	return when (this) {
		SessionResetNoticeReason.ServerEndedSession -> getString(Res.string.snack_session_invalidated)
		SessionResetNoticeReason.UnreadableData -> getString(Res.string.snack_session_reset_on_startup)
	}
}
