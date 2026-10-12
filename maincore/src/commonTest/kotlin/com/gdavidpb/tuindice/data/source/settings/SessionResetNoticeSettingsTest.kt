package com.gdavidpb.tuindice.data.source.settings

import com.gdavidpb.tuindice.base.domain.model.SessionResetNoticeReason
import com.gdavidpb.tuindice.testing.FakeSettings
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

// The notice owed to the user keeps WHY the session was reset, so that a process that dies before the
// interface shows it still tells the truth at the next start.
class SessionResetNoticeSettingsTest {
	@Test
	fun theReasonIsKept_andConsumingItTakesIt() = runTest {
		val dataSource = MultiplatformSettingsDataSource(FakeSettings())

		assertNull(dataSource.getSessionResetNoticeReason())

		dataSource.setSessionResetNoticePending(SessionResetNoticeReason.ServerEndedSession)
		assertEquals(SessionResetNoticeReason.ServerEndedSession, dataSource.getSessionResetNoticeReason())

		dataSource.setSessionResetNoticePending(SessionResetNoticeReason.UnreadableData)
		assertEquals(SessionResetNoticeReason.UnreadableData, dataSource.getSessionResetNoticeReason())

		assertTrue(dataSource.consumeSessionResetNoticePending())
		assertNull(dataSource.getSessionResetNoticeReason())
		assertFalse(dataSource.consumeSessionResetNoticePending())
	}

	// What the versions before the reason was kept left behind: a flag that only an unreadable start-up set.
	@Test
	fun aFlagLeftByAnOlderVersion_isReadAsUnreadableData_andConsumedWithIt() = runTest {
		val settings = FakeSettings()
		settings.putBoolean("sessionResetNoticePending", true)
		val dataSource = MultiplatformSettingsDataSource(settings)

		assertEquals(SessionResetNoticeReason.UnreadableData, dataSource.getSessionResetNoticeReason())
		assertTrue(dataSource.consumeSessionResetNoticePending())
		assertNull(dataSource.getSessionResetNoticeReason())
	}
}
