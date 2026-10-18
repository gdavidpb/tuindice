package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.model.AssistantActiveTurn
import com.gdavidpb.tuindice.assistant.domain.model.AssistantTurnPhase
import com.gdavidpb.tuindice.assistant.domain.usecase.param.FollowAssistantTurnParams
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantConversationRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantServerDisabledRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantTurnRepository
import com.gdavidpb.tuindice.assistant.testing.dataValues
import com.gdavidpb.tuindice.assistant.testing.lastState
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent
import com.gdavidpb.tuindice.chatcontract.event.Progress
import com.gdavidpb.tuindice.chatcontract.event.RunError
import com.gdavidpb.tuindice.chatcontract.event.RunErrorCode
import com.gdavidpb.tuindice.chatcontract.event.RunFinished
import com.gdavidpb.tuindice.chatcontract.event.RunStarted
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AssistantTurnUseCasesTest {
	private val calls = mutableListOf<String>()
	private val turns = RecordingAssistantTurnRepository(calls)
	private val conversation = RecordingAssistantConversationRepository(calls)
	private val disabled = RecordingAssistantServerDisabledRepository()
	private val stream = AssistantTurnStream(turns, conversation, disabled)

	@Test
	fun eachEventReachesTheConversationInOrderAndIsEmitted() = runTest {
		val events = listOf(runStarted(1), progress(2), runFinished(3))
		turns.events = events

		val emitted = FollowAssistantTurnUseCase(stream, RecordingReportingRepository())
			.execute(FollowAssistantTurnParams(turnId = "t1", afterSeq = 0))
			.dataValues()

		assertEquals(events, conversation.applied)
		assertEquals(events, emitted)
		assertEquals(listOf("t1" to 0L), turns.followed)
	}

	@Test
	fun aRunErrorOfADisabledAssistantHoldsItOffForItsRetryTime() = runTest {
		val refused = RunError(
			seq = 2,
			turnId = "t1",
			code = RunErrorCode.ASSISTANT_DISABLED,
			retryable = false,
			retryAfterSeconds = 120
		)
		turns.events = listOf(runStarted(1), refused)

		FollowAssistantTurnUseCase(stream, RecordingReportingRepository())
			.execute(FollowAssistantTurnParams(turnId = "t1", afterSeq = 0))
			.dataValues()

		assertEquals(listOf<Int?>(120), disabled.marks)
		assertEquals(listOf<AssistantEvent>(runStarted(1), refused), conversation.applied)
	}

	@Test
	fun otherRunErrorsDoNotHoldTheAssistantOff() = runTest {
		turns.events = listOf(RunError(seq = 1, turnId = "t1", code = RunErrorCode.TRANSIENT, retryable = true))

		FollowAssistantTurnUseCase(stream, RecordingReportingRepository())
			.execute(FollowAssistantTurnParams(turnId = "t1", afterSeq = 0))
			.dataValues()

		assertTrue(disabled.marks.isEmpty())
	}

	@Test
	fun resumeFollowsTheRunningTurnFromTheSeqItReached() = runTest {
		conversation.activeTurn = AssistantActiveTurn(
			turnId = "t1",
			lastSeq = 4,
			progressLabel = null,
			status = AssistantTurnPhase.RUNNING
		)
		turns.events = listOf(runFinished(5))

		ResumeAssistantTurnUseCase(stream, conversation, RecordingReportingRepository())
			.execute(Unit)
			.dataValues()

		assertEquals(listOf("t1" to 4L), turns.followed)
		assertEquals(listOf<AssistantEvent>(runFinished(5)), conversation.applied)
	}

	@Test
	fun resumeDoesNothingWhenTheLastTurnIsNotRunning() = runTest {
		conversation.activeTurn = AssistantActiveTurn(
			turnId = "t1",
			lastSeq = 5,
			progressLabel = null,
			status = AssistantTurnPhase.FINISHED
		)

		val emitted = ResumeAssistantTurnUseCase(stream, conversation, RecordingReportingRepository())
			.execute(Unit)
			.dataValues()

		assertTrue(emitted.isEmpty())
		assertTrue(turns.followed.isEmpty())
	}

	@Test
	fun aStreamThatEndsBeforeItsFinalEventIsCheckedFromTheLastSeq_andLeftUnconfirmedWhenTheCheckFindsNothingEither() = runTest {
		turns.events = listOf(runStarted(1), progress(2))

		val emitted = FollowAssistantTurnUseCase(stream, RecordingReportingRepository())
			.execute(FollowAssistantTurnParams(turnId = "t1", afterSeq = 0))
			.dataValues()

		assertEquals(listOf("t1" to 0L, "t1" to 2L), turns.followed)
		assertEquals(listOf<AssistantEvent>(runStarted(1), progress(2)), emitted)
		assertEquals(listOf("t1"), conversation.unconfirmed)
	}

	@Test
	fun aCheckThatReachesTheFinalEventLeavesTheTurnConfirmed() = runTest {
		turns.scriptedFollows += listOf(listOf(runStarted(1)), listOf(progress(2), runFinished(3)))

		val emitted = FollowAssistantTurnUseCase(stream, RecordingReportingRepository())
			.execute(FollowAssistantTurnParams(turnId = "t1", afterSeq = 0))
			.dataValues()

		assertEquals(listOf("t1" to 0L, "t1" to 1L), turns.followed)
		assertEquals(listOf<AssistantEvent>(runStarted(1), progress(2), runFinished(3)), emitted)
		assertTrue(conversation.unconfirmed.isEmpty())
	}

	@Test
	fun aStreamThatBreaksIsCheckedLeftUnconfirmed_andItsFailureIsStillReported() = runTest {
		turns.streamError = IllegalStateException("cut")

		val state = FollowAssistantTurnUseCase(stream, RecordingReportingRepository())
			.execute(FollowAssistantTurnParams(turnId = "t1", afterSeq = 0))
			.lastState()

		assertEquals(listOf("t1" to 0L, "t1" to 0L), turns.followed)
		assertEquals(listOf("t1"), conversation.unconfirmed)
		assertTrue(state is UseCaseState.Error)
	}

	@Test
	fun resumeFollowsAnUnconfirmedTurnToLearnWhereItStands() = runTest {
		conversation.activeTurn = AssistantActiveTurn(
			turnId = "t1",
			lastSeq = 4,
			progressLabel = null,
			status = AssistantTurnPhase.UNCONFIRMED
		)
		turns.events = listOf(runFinished(5))

		ResumeAssistantTurnUseCase(stream, conversation, RecordingReportingRepository())
			.execute(Unit)
			.dataValues()

		assertEquals(listOf("t1" to 4L), turns.followed)
		assertEquals(listOf<AssistantEvent>(runFinished(5)), conversation.applied)
	}

	@Test
	fun cancelAsksTheServerToCancelTheTurn() = runTest {
		CancelAssistantTurnUseCase(turns, RecordingReportingRepository()).execute("t1").dataValues()

		assertEquals(listOf("t1"), turns.cancelled)
	}

	private fun runStarted(seq: Long) = RunStarted(seq = seq, turnId = "t1")

	private fun progress(seq: Long) = Progress(seq = seq, turnId = "t1", tool = "subjects", label = "Buscando materias")

	private fun runFinished(seq: Long) = RunFinished(seq = seq, turnId = "t1")
}
