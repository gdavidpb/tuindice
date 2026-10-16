package com.gdavidpb.tuindice.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.SessionResetNotice
import com.gdavidpb.tuindice.presentation.model.SessionNoticeText

// The notice a reset session left, as the machine takes it: its stamp, and the text the user is told. The live
// signal and the start-up both map through here, so one notice reads the same whichever way it arrives.
internal suspend fun SessionResetNotice.toSessionNoticeText(): SessionNoticeText {
	return SessionNoticeText(stamp = stamp, message = reason.toNoticeText())
}
