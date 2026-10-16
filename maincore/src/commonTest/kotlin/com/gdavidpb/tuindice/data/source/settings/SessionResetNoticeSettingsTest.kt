package com.gdavidpb.tuindice.data.source.settings

import com.gdavidpb.tuindice.base.domain.model.SessionResetNoticeReason
import com.gdavidpb.tuindice.testing.FakeSettings
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

// The notice owed to the user keeps WHY the session was reset, so that a process that dies before the
// interface shows it still tells the truth at the next start, and a stamp that says WHICH notice it is.
class SessionResetNoticeSettingsTest {
	@Test
	fun theReasonIsKept_andConsumingItByItsStampTakesIt() = runTest {
		val dataSource = MultiplatformSettingsDataSource(FakeSettings())

		assertNull(dataSource.getSessionResetNotice())

		val first = dataSource.setSessionResetNoticePending(SessionResetNoticeReason.ServerEndedSession)
		assertEquals(first, dataSource.getSessionResetNotice())

		val second = dataSource.setSessionResetNoticePending(SessionResetNoticeReason.UnreadableData)
		assertEquals(second, dataSource.getSessionResetNotice())
		assertNotEquals(first.stamp, second.stamp, "each notice has its own stamp")

		assertTrue(dataSource.consumeSessionResetNotice(second.stamp))
		assertNull(dataSource.getSessionResetNotice())
		assertFalse(dataSource.consumeSessionResetNotice(second.stamp))
	}

	// Taking the notice that was shown must not take a newer one persisted in the meantime.
	@Test
	fun consumingByAnOldStamp_leavesTheNewerNoticePending() = runTest {
		val dataSource = MultiplatformSettingsDataSource(FakeSettings())
		val old = dataSource.setSessionResetNoticePending(SessionResetNoticeReason.ServerEndedSession)
		val newer = dataSource.setSessionResetNoticePending(SessionResetNoticeReason.ServerEndedSession)

		assertFalse(dataSource.consumeSessionResetNotice(old.stamp))
		assertEquals(newer, dataSource.getSessionResetNotice())
	}

	// What the versions before the reason was kept left behind: a flag that only an unreadable start-up set.
	@Test
	fun aFlagLeftByAnOlderVersion_isReadAsUnreadableDataWithAFixedStamp_andConsumedWithIt() = runTest {
		val settings = FakeSettings()
		settings.putBoolean("sessionResetNoticePending", true)
		val dataSource = MultiplatformSettingsDataSource(settings)

		val notice = dataSource.getSessionResetNotice()
		assertEquals(SessionResetNoticeReason.UnreadableData, notice?.reason)
		assertEquals(notice, dataSource.getSessionResetNotice(), "the fixed stamp is the same every time")
		assertTrue(dataSource.consumeSessionResetNotice(notice!!.stamp))
		assertNull(dataSource.getSessionResetNotice())
	}

	// The reason alone, as the versions that kept one but no stamp left it.
	@Test
	fun aReasonLeftWithoutAStamp_isReadWithTheFixedOne() = runTest {
		val settings = FakeSettings()
		settings.putString("sessionResetNoticeReason", SessionResetNoticeReason.ServerEndedSession.name)
		val dataSource = MultiplatformSettingsDataSource(settings)

		val notice = dataSource.getSessionResetNotice()
		assertEquals(SessionResetNoticeReason.ServerEndedSession, notice?.reason)
		assertTrue(dataSource.consumeSessionResetNotice(notice!!.stamp))
		assertNull(dataSource.getSessionResetNotice())
	}
}
