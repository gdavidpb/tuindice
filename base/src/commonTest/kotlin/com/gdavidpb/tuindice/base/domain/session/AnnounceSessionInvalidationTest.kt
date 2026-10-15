package com.gdavidpb.tuindice.base.domain.session

import com.gdavidpb.tuindice.base.domain.model.SessionResetNotice
import com.gdavidpb.tuindice.base.domain.model.SessionResetNoticeReason
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionInvalidationRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AnnounceSessionInvalidationTest {
	@Test
	fun whenTheSignalIsRaised_theNoticeIsWrittenBeforeTheSignalAndKept() = runTest {
		val events = mutableListOf<String>()
		val settings = RecordingSettingsRepository(FakeSettingsRepository(), events)
		val invalidation = RecordingSessionInvalidationRepository(events, raised = true)

		invalidation.announceInvalidation(settings, SessionResetNoticeReason.UnreadableData, sessionId = "session-1")

		assertEquals(listOf("notice", "signal"), events)
		assertNotNull(settings.getSessionResetNotice())
	}

	@Test
	fun whenTheSignalIsSuppressedBySignOut_theNoticeIsWrittenThenTakenBack() = runTest {
		val events = mutableListOf<String>()
		val settings = RecordingSettingsRepository(FakeSettingsRepository(), events)
		val invalidation = RecordingSessionInvalidationRepository(events, raised = false)

		invalidation.announceInvalidation(settings, SessionResetNoticeReason.UnreadableData, sessionId = "session-1")

		assertEquals(listOf("notice", "signal", "consume"), events)
		assertNull(settings.getSessionResetNotice())
	}

	@Test
	fun whenTheNoticeCannotBeWritten_theSignalIsStillRaisedAndNothingIsTakenBack() = runTest {
		val events = mutableListOf<String>()
		val settings = RecordingSettingsRepository(FakeSettingsRepository(), events, failsToWrite = true)
		val invalidation = RecordingSessionInvalidationRepository(events, raised = false)

		invalidation.announceInvalidation(settings, SessionResetNoticeReason.UnreadableData, sessionId = null)

		assertEquals(listOf("signal"), events)
	}

	private class RecordingSettingsRepository(
		private val delegate: SettingsRepository,
		private val events: MutableList<String>,
		private val failsToWrite: Boolean = false
	) : SettingsRepository by delegate {
		override suspend fun setSessionResetNoticePending(reason: SessionResetNoticeReason): SessionResetNotice {
			check(!failsToWrite) { "the secure store is unavailable" }

			events += "notice"

			return delegate.setSessionResetNoticePending(reason)
		}

		override suspend fun consumeSessionResetNotice(stamp: Long): Boolean {
			events += "consume"

			return delegate.consumeSessionResetNotice(stamp)
		}
	}

	private class RecordingSessionInvalidationRepository(
		private val events: MutableList<String>,
		private val raised: Boolean
	) : SessionInvalidationRepository by FakeSessionInvalidationRepository() {
		override fun notifySessionInvalidated(sessionId: String?): Boolean {
			events += "signal"

			return raised
		}
	}
}
