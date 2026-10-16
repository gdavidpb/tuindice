package com.gdavidpb.tuindice.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.SessionResetNotice
import com.gdavidpb.tuindice.base.domain.model.SessionResetNoticeReason
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import tuindice.maincore.generated.resources.Res
import tuindice.maincore.generated.resources.snack_session_invalidated
import tuindice.maincore.generated.resources.snack_session_reset_on_startup
import kotlin.test.Test
import kotlin.test.assertEquals

// The texts come from the compose resources, which the host tests cannot read, so the mapping is checked on the
// simulator.
class SessionNoticeTextMapperTest {
	@Test
	fun aServerEndedSession_keepsTheStampAndTellsThatTheSessionEnded() = runTest {
		val notice = SessionResetNotice(reason = SessionResetNoticeReason.ServerEndedSession, stamp = 42L)

		val text = notice.toSessionNoticeText()

		assertEquals(42L, text.stamp)
		assertEquals(getString(Res.string.snack_session_invalidated), text.message)
	}

	@Test
	fun unreadableSessionData_keepsTheStampAndTellsTheStartUpText() = runTest {
		val notice = SessionResetNotice(reason = SessionResetNoticeReason.UnreadableData, stamp = 7L)

		val text = notice.toSessionNoticeText()

		assertEquals(7L, text.stamp)
		assertEquals(getString(Res.string.snack_session_reset_on_startup), text.message)
	}
}
