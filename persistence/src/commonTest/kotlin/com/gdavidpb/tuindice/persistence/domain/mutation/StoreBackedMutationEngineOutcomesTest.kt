package com.gdavidpb.tuindice.persistence.domain.mutation

import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcome
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcomeKind
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class StoreBackedMutationEngineOutcomesTest {
	@Test
	fun outcomes_whenAMutationIsConfirmed_publishesConfirmed() = runTest {
		val engine = createEngine(InMemoryMutationEnvelopeStore(), this)
		val outcomes = collectOutcomes(engine)

		engine.submit(testMutationEnvelope(mutationId = "mutation-1", value = 10), ScriptedSpec())

		assertEquals(listOf(MutationOutcome("mutation-1", MutationOutcomeKind.CONFIRMED)), outcomes)
	}

	@Test
	fun outcomes_whenAConflictDropsTheRow_publishesConflictWithTheKindAsDetail() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		val outcomes = collectOutcomes(engine)

		engine.submit(
			testMutationEnvelope(mutationId = "mutation-1", value = 10),
			FailingSpec(kind = MutationFailureKind.Conflict) { MutationFailureResolution.Drop() },
			propagateTerminalErrors = false
		)

		assertEquals(emptyList(), store.getPendingMutations("record"))
		assertEquals(
			listOf(MutationOutcome("mutation-1", MutationOutcomeKind.CONFLICT, detailCode = "Conflict")),
			outcomes
		)
	}

	@Test
	fun outcomes_whenAPreconditionFailureDropsTheRow_publishesConflict() = runTest {
		val engine = createEngine(InMemoryMutationEnvelopeStore(), this)
		val outcomes = collectOutcomes(engine)

		engine.submit(
			testMutationEnvelope(mutationId = "mutation-1", value = 10),
			FailingSpec(kind = MutationFailureKind.PreconditionFailed) { MutationFailureResolution.Drop() },
			propagateTerminalErrors = false
		)

		assertEquals(
			listOf(
				MutationOutcome(
					"mutation-1",
					MutationOutcomeKind.CONFLICT,
					detailCode = "PreconditionFailed"
				)
			),
			outcomes
		)
	}

	@Test
	fun outcomes_whenANotFoundDropsTheRow_publishesRejected() = runTest {
		val engine = createEngine(InMemoryMutationEnvelopeStore(), this)
		val outcomes = collectOutcomes(engine)

		engine.submit(
			testMutationEnvelope(mutationId = "mutation-1", value = 10),
			FailingSpec(kind = MutationFailureKind.NotFound) {
				MutationFailureResolution.Drop(propagate = true)
			},
			propagateTerminalErrors = false
		)

		assertEquals(
			listOf(MutationOutcome("mutation-1", MutationOutcomeKind.REJECTED, detailCode = "NotFound")),
			outcomes
		)
	}

	@Test
	fun outcomes_whenAFailureFailsTheRow_publishesRejectedOrConflictByKind() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		val outcomes = collectOutcomes(engine)

		engine.submit(
			testMutationEnvelope(mutationId = "mutation-1", value = 10),
			FailingSpec(kind = MutationFailureKind.Terminal) { MutationFailureResolution.Fail() },
			propagateTerminalErrors = false
		)
		engine.submit(
			testMutationEnvelope(mutationId = "mutation-2", value = 20),
			FailingSpec(kind = MutationFailureKind.PreconditionFailed) { MutationFailureResolution.Fail() },
			propagateTerminalErrors = false
		)

		assertEquals(
			listOf(
				MutationOutcome("mutation-1", MutationOutcomeKind.REJECTED, detailCode = "Terminal"),
				MutationOutcome("mutation-2", MutationOutcomeKind.CONFLICT, detailCode = "PreconditionFailed")
			),
			outcomes
		)
		assertEquals(
			PendingMutationStatus.FailedTerminal,
			store.getMutations("record").single { row -> row.mutationId == "mutation-1" }.status
		)
	}

	@Test
	fun outcomes_whenAFailureIsDeferred_publishesNothing() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		val outcomes = collectOutcomes(engine)

		engine.submit(
			testMutationEnvelope(mutationId = "mutation-1", value = 10),
			FailingSpec(kind = MutationFailureKind.Terminal) { MutationFailureResolution.Defer() },
			propagateTerminalErrors = false
		)

		assertEquals(emptyList(), outcomes)
		assertEquals(
			PendingMutationStatus.Pending,
			store.getPendingMutations("record").single().status
		)
	}

	@Test
	fun outcomes_whenARebaseIsParkedForGood_publishesConflict() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		val outcomes = collectOutcomes(engine)

		engine.submit(
			testMutationEnvelope(mutationId = "mutation-1", value = 10),
			FailingSpec(
				kind = MutationFailureKind.Conflict,
				exhaustedLimit = 1
			) { mutation -> MutationFailureResolution.Retry(mutation) },
			propagateTerminalErrors = false
		)

		assertEquals(
			listOf(MutationOutcome("mutation-1", MutationOutcomeKind.CONFLICT, detailCode = "Conflict")),
			outcomes
		)
		assertEquals(
			PendingMutationStatus.FailedTerminal,
			store.getMutations("record").single().status
		)
	}

	@Test
	fun outcomes_whenARebaseIsParkedAsFailed_publishesNothing() = runTest {
		val engine = createEngine(InMemoryMutationEnvelopeStore(), this)
		val outcomes = collectOutcomes(engine)

		engine.submit(
			testMutationEnvelope(mutationId = "mutation-1", value = 10),
			FailingSpec(kind = MutationFailureKind.Conflict) { mutation ->
				MutationFailureResolution.Retry(mutation)
			},
			propagateTerminalErrors = false
		)

		assertEquals(emptyList(), outcomes)
	}

	@Test
	fun outcomes_whenAReplacedRowWasWaitingWithoutExecuting_publishesSupersededForIt() = runTest {
		val waitingRow = testMutationEnvelope(mutationId = "mutation-1", value = 10, replaceKey = "subject:1")
			.copy(status = PendingMutationStatus.Failed)
		val store = InMemoryMutationEnvelopeStore(listOf(waitingRow))
		val engine = createEngine(store, this)
		val outcomes = collectOutcomes(engine)

		engine.submit(
			testMutationEnvelope(mutationId = "mutation-2", value = 20, replaceKey = "subject:1"),
			ScriptedSpec()
		)

		assertEquals(
			listOf(
				MutationOutcome("mutation-1", MutationOutcomeKind.SUPERSEDED),
				MutationOutcome("mutation-2", MutationOutcomeKind.CONFIRMED)
			),
			outcomes
		)
	}

	@Test
	fun outcomes_whenAReplacedMutationWasInFlight_publishesSupersededOnceAndNoStaleConfirmation() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		val outcomes = collectOutcomes(engine)
		val firstSendStarted = CompletableDeferred<Unit>()
		val releaseFirstAck = CompletableDeferred<Unit>()
		val syncSpec = object : MutationSyncSpec<String, TestMutation, TestAck> {
			override suspend fun send(
				mutation: MutationEnvelope<String, TestMutation>
			): TestAck {
				if (mutation.mutationId == "mutation-1") {
					firstSendStarted.complete(Unit)
					releaseFirstAck.await()
				}
				return TestAck(mutation.mutationId, mutation.command.value)
			}

			override suspend fun confirm(
				mutation: MutationEnvelope<String, TestMutation>,
				ack: TestAck
			) = Unit
		}

		val firstJob = launch {
			engine.submit(testMutationEnvelope(mutationId = "mutation-1", value = 10, replaceKey = "subject:1"), syncSpec)
		}
		firstSendStarted.await()
		val secondJob = launch {
			engine.submit(testMutationEnvelope(mutationId = "mutation-2", value = 20, replaceKey = "subject:1"), syncSpec)
		}
		yield()
		releaseFirstAck.complete(Unit)
		firstJob.join()
		secondJob.join()

		assertEquals(
			listOf(
				MutationOutcome("mutation-1", MutationOutcomeKind.SUPERSEDED),
				MutationOutcome("mutation-2", MutationOutcomeKind.CONFIRMED)
			),
			outcomes
		)
	}

	@Test
	fun outcomes_whenAReplacedRowIsTerminal_publishesNothingForIt() = runTest {
		val terminalRow = testMutationEnvelope(mutationId = "mutation-1", value = 10, replaceKey = "subject:1")
			.copy(status = PendingMutationStatus.FailedTerminal)
		val store = InMemoryMutationEnvelopeStore(listOf(terminalRow))
		val engine = createEngine(store, this)
		val outcomes = collectOutcomes(engine)

		engine.submit(
			testMutationEnvelope(mutationId = "mutation-2", value = 20, replaceKey = "subject:1"),
			ScriptedSpec()
		)

		assertEquals(listOf(MutationOutcome("mutation-2", MutationOutcomeKind.CONFIRMED)), outcomes)
	}

	@Test
	fun outcomes_whenAWaitingRowIsDeleted_publishesCancelled() = runTest {
		val store = InMemoryMutationEnvelopeStore(listOf(testMutationEnvelope(mutationId = "mutation-1", value = 10)))
		val engine = createEngine(store, this)
		val outcomes = collectOutcomes(engine)

		engine.deletePendingMutation(scopeKey = "record", mutationId = "mutation-1")

		assertEquals(listOf(MutationOutcome("mutation-1", MutationOutcomeKind.CANCELLED)), outcomes)
	}

	@Test
	fun outcomes_whenATerminalRowOrAnUnknownOneIsDeleted_publishesNothing() = runTest {
		val terminalRow = testMutationEnvelope(mutationId = "mutation-1", value = 10)
			.copy(status = PendingMutationStatus.FailedTerminal)
		val store = InMemoryMutationEnvelopeStore(listOf(terminalRow))
		val engine = createEngine(store, this)
		val outcomes = collectOutcomes(engine)

		engine.deletePendingMutation(scopeKey = "record", mutationId = "mutation-1")
		engine.deletePendingMutation(scopeKey = "record", mutationId = "unknown")

		assertEquals(emptyList(), outcomes)
	}

	@Test
	fun outcomes_whenTheRowIsDeletedWhileItsFailureIsResolved_publishesOnlyTheCancellation() = runTest {
		val store = InMemoryMutationEnvelopeStore(listOf(testMutationEnvelope(mutationId = "mutation-1", value = 10)))
		val engine = createEngine(store, this)
		val outcomes = collectOutcomes(engine)
		val resolutionStarted = CompletableDeferred<Unit>()
		val releaseResolution = CompletableDeferred<Unit>()
		val syncSpec = object : MutationSyncSpec<String, TestMutation, TestAck> {
			override suspend fun send(
				mutation: MutationEnvelope<String, TestMutation>
			): TestAck = throw TestTerminalFailure()

			override suspend fun confirm(
				mutation: MutationEnvelope<String, TestMutation>,
				ack: TestAck
			) = Unit

			override fun classifyError(
				mutation: MutationEnvelope<String, TestMutation>,
				throwable: Throwable
			) = MutationFailureKind.Conflict

			override suspend fun resolveFailure(
				mutation: MutationEnvelope<String, TestMutation>,
				throwable: Throwable
			): MutationFailureResolution<String, TestMutation> {
				resolutionStarted.complete(Unit)
				releaseResolution.await()
				return MutationFailureResolution.Drop()
			}
		}

		val job = launch {
			engine.submit(
				testMutationEnvelope(mutationId = "mutation-1", value = 10),
				syncSpec,
				propagateTerminalErrors = false
			)
		}
		resolutionStarted.await()
		engine.deletePendingMutation(scopeKey = "record", mutationId = "mutation-1")
		releaseResolution.complete(Unit)
		job.join()

		assertEquals(listOf(MutationOutcome("mutation-1", MutationOutcomeKind.CANCELLED)), outcomes)
	}

	@Test
	fun outcomes_whenARetryIsResolvedAfterItsRowWasReplaced_doesNotWriteTheRowBackNorSendItAgain() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val spec = ReplacedRowSpec { mutation ->
			MutationFailureResolution.Retry(mutation.copy(precondition = MutationPrecondition.Revision(2L)))
		}

		val race = submitSecondWhileFirstIsHeld(store, spec, spec.resolutionStarted) { spec.releaseResolution() }
		race.engine.drain(scopeKey = "record", syncSpec = spec)
		advanceUntilIdle()
		val outcomes = race.outcomes

		assertEquals(emptyList(), store.getMutations("record"))
		assertEquals(listOf("mutation-1", "mutation-2"), spec.sentIds)
		assertEquals(supersededThenConfirmed(), outcomes)
	}

	@Test
	fun outcomes_whenAFailIsResolvedAfterItsRowWasReplaced_doesNotWriteTheRowBackNorPublishesAgain() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val spec = ReplacedRowSpec { MutationFailureResolution.Fail() }

		val race = submitSecondWhileFirstIsHeld(store, spec, spec.resolutionStarted) { spec.releaseResolution() }
		race.engine.drain(scopeKey = "record", syncSpec = spec)
		advanceUntilIdle()
		val outcomes = race.outcomes

		assertEquals(emptyList(), store.getMutations("record"))
		assertEquals(listOf("mutation-1", "mutation-2"), spec.sentIds)
		assertEquals(supersededThenConfirmed(), outcomes)
	}

	@Test
	fun outcomes_whenADeferIsResolvedAfterItsRowWasReplaced_doesNotWriteTheRowBack() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val spec = ReplacedRowSpec { MutationFailureResolution.Defer() }

		val race = submitSecondWhileFirstIsHeld(store, spec, spec.resolutionStarted) { spec.releaseResolution() }
		race.engine.drain(scopeKey = "record", syncSpec = spec)
		advanceUntilIdle()
		val outcomes = race.outcomes

		assertEquals(emptyList(), store.getMutations("record"))
		assertEquals(listOf("mutation-1", "mutation-2"), spec.sentIds)
		assertEquals(supersededThenConfirmed(), outcomes)
	}

	@Test
	fun outcomes_whenADeferIsResolvedAfterItsRowWasDiscarded_doesNotWriteTheRowBack() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		val outcomes = collectOutcomes(engine)
		val spec = ReplacedRowSpec { MutationFailureResolution.Defer() }

		val first = launch {
			engine.submit(
				testMutationEnvelope(mutationId = "mutation-1", value = 10, replaceKey = "subject:1"),
				spec,
				propagateTerminalErrors = false
			)
		}
		spec.resolutionStarted.await()
		engine.discardMutation(scopeKey = "record", mutationId = "mutation-1")
		spec.releaseResolution()
		first.join()
		engine.drain(scopeKey = "record", syncSpec = spec)
		advanceUntilIdle()

		assertEquals(emptyList(), store.getMutations("record"))
		assertEquals(listOf("mutation-1"), spec.sentIds)
		assertEquals(listOf(MutationOutcome("mutation-1", MutationOutcomeKind.CANCELLED)), outcomes)
	}

	@Test
	fun outcomes_whenTheResolverFailsAfterItsRowWasReplaced_doesNotSaveTheRowAsFailed() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val spec = ReplacedRowSpec { throw TestTerminalFailure() }

		val race = submitSecondWhileFirstIsHeld(store, spec, spec.resolutionStarted) { spec.releaseResolution() }
		race.engine.drain(scopeKey = "record", syncSpec = spec)
		advanceUntilIdle()
		val outcomes = race.outcomes

		assertEquals(emptyList(), store.getMutations("record"))
		assertEquals(listOf("mutation-1", "mutation-2"), spec.sentIds)
		assertEquals(supersededThenConfirmed(), outcomes)
	}

	@Test
	fun outcomes_whenAPreconditionIsAlignedAfterItsRowWasReplaced_doesNotSaveTheRowBackNorSendIt() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val spec = GatedPreconditionSpec()

		val race = submitSecondWhileFirstIsHeld(store, spec, spec.preconditionStarted) { spec.releasePrecondition() }
		assertEquals(emptyList(), store.getMutations("record"))

		race.engine.drain(scopeKey = "record", syncSpec = spec)
		advanceUntilIdle()

		assertEquals(emptyList(), store.getMutations("record"))
		assertEquals(listOf("mutation-2"), spec.sentIds)
		assertEquals(supersededThenConfirmed(), race.outcomes)
	}

	@Test
	fun outcomes_whenAWaitingRowIsDiscarded_publishesCancelledAtOnce() = runTest {
		val store = InMemoryMutationEnvelopeStore(listOf(testMutationEnvelope(mutationId = "mutation-1", value = 10)))
		val engine = createEngine(store, this)
		val outcomes = collectOutcomes(engine)

		engine.discardMutation(scopeKey = "record", mutationId = "mutation-1")

		assertEquals(emptyList(), store.getMutations("record"))
		assertEquals(listOf(MutationOutcome("mutation-1", MutationOutcomeKind.CANCELLED)), outcomes)
	}

	@Test
	fun outcomes_whenAFailedRowIsDiscarded_publishesCancelled() = runTest {
		val failedRow = testMutationEnvelope(mutationId = "mutation-1", value = 10)
			.copy(status = PendingMutationStatus.Failed)
		val store = InMemoryMutationEnvelopeStore(listOf(failedRow))
		val engine = createEngine(store, this)
		val outcomes = collectOutcomes(engine)

		engine.discardMutation(scopeKey = "record", mutationId = "mutation-1")

		assertEquals(emptyList(), store.getMutations("record"))
		assertEquals(listOf(MutationOutcome("mutation-1", MutationOutcomeKind.CANCELLED)), outcomes)
	}

	@Test
	fun outcomes_whenATerminalRowIsDiscarded_publishesNothingForIt() = runTest {
		val terminalRow = testMutationEnvelope(mutationId = "mutation-1", value = 10)
			.copy(status = PendingMutationStatus.FailedTerminal)
		val store = InMemoryMutationEnvelopeStore(listOf(terminalRow))
		val engine = createEngine(store, this)
		val outcomes = collectOutcomes(engine)

		engine.discardMutation(scopeKey = "record", mutationId = "mutation-1")

		assertEquals(emptyList(), store.getMutations("record"))
		assertEquals(emptyList(), outcomes)
	}

	// The row is discarded while its send is in flight, and the send then succeeds: its acknowledgement arrives for a row
	// that is gone. The result is CANCELLED alone: no CONFIRMED follows it, and the spec's confirm does not run.
	@Test
	fun outcomes_whenARowIsDiscardedWhileItsSendIsInFlight_publishesOnlyCancelledAndNoLateConfirmation() = runTest {
		val store = InMemoryMutationEnvelopeStore(listOf(testMutationEnvelope(mutationId = "mutation-1", value = 10)))
		val engine = createEngine(store, this)
		val outcomes = collectOutcomes(engine)
		val sendStarted = CompletableDeferred<Unit>()
		val releaseAck = CompletableDeferred<Unit>()
		val spec = ScriptedSpec(
			onSend = {
				sendStarted.complete(Unit)
				releaseAck.await()
			}
		)

		val job = launch {
			engine.submit(testMutationEnvelope(mutationId = "mutation-1", value = 10), spec)
		}
		sendStarted.await()
		engine.discardMutation(scopeKey = "record", mutationId = "mutation-1")
		releaseAck.complete(Unit)
		job.join()
		advanceUntilIdle()

		assertEquals(emptyList(), store.getMutations("record"))
		assertEquals(0, spec.confirmCount)
		assertEquals(listOf(MutationOutcome("mutation-1", MutationOutcomeKind.CANCELLED)), outcomes)
	}

	@Test
	fun publishLocalOutcome_reachesTheSubscribersFromThenOn_andIsNotReplayed() = runTest {
		val engine = createEngine(InMemoryMutationEnvelopeStore(), this)

		engine.publishLocalOutcome(MutationOutcome("before", MutationOutcomeKind.CONFIRMED))
		val outcomes = collectOutcomes(engine)
		engine.publishLocalOutcome(MutationOutcome("after", MutationOutcomeKind.CONFIRMED))
		runCurrent()

		assertEquals(listOf(MutationOutcome("after", MutationOutcomeKind.CONFIRMED)), outcomes)
	}

	private fun TestScope.collectOutcomes(
		engine: StoreBackedMutationEngine<String, TestMutation, TestAck>
	): MutableList<MutationOutcome> {
		val collected = mutableListOf<MutationOutcome>()

		backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
			engine.outcomes.collect { outcome -> collected += outcome }
		}

		return collected
	}

	private fun supersededThenConfirmed() = listOf(
		MutationOutcome("mutation-1", MutationOutcomeKind.SUPERSEDED),
		MutationOutcome("mutation-2", MutationOutcomeKind.CONFIRMED)
	)

	// Submits "mutation-1" and waits until [held] completes, which is when its spec holds it. Then it submits
	// "mutation-2" for the same subject, which replaces the first row, and lets "mutation-1" go on through [release].
	// Returns once both have settled: the test drains the outbox afterwards, as a later sync would.
	private suspend fun TestScope.submitSecondWhileFirstIsHeld(
		store: InMemoryMutationEnvelopeStore<String, TestMutation>,
		spec: MutationSyncSpec<String, TestMutation, TestAck>,
		held: CompletableDeferred<Unit>,
		release: () -> Unit
	): Race {
		val engine = createEngine(store, this)
		val outcomes = collectOutcomes(engine)

		val firstVersion = engine.beginMutation(replaceKey = "subject:1")
		engine.rememberMutationVersion("mutation-1", firstVersion)
		val first = launch {
			engine.submit(
				testMutationEnvelope(mutationId = "mutation-1", value = 10, replaceKey = "subject:1"),
				spec,
				propagateTerminalErrors = false
			)
		}
		held.await()

		val secondVersion = engine.beginMutation(replaceKey = "subject:1")
		engine.rememberMutationVersion("mutation-2", secondVersion)
		engine.submitInBackground(
			testMutationEnvelope(mutationId = "mutation-2", value = 20, replaceKey = "subject:1"),
			spec
		)

		release()
		first.join()
		advanceUntilIdle()

		return Race(engine, outcomes)
	}

	// What a race leaves behind: the engine, and the outcomes published so far (a live list).
	private class Race(
		val engine: StoreBackedMutationEngine<String, TestMutation, TestAck>,
		val outcomes: List<MutationOutcome>
	)

	// "mutation-1" is sent and fails with a conflict; every other mutation is acknowledged. The failure of "mutation-1"
	// is resolved by [onResolve], once the test releases the resolution.
	private class ReplacedRowSpec(
		private val onResolve: (MutationEnvelope<String, TestMutation>) -> MutationFailureResolution<String, TestMutation>
	) : MutationSyncSpec<String, TestMutation, TestAck> {
		val resolutionStarted = CompletableDeferred<Unit>()
		private val resolutionReleased = CompletableDeferred<Unit>()
		val sentIds = mutableListOf<String>()

		override val maxRebaseAttempts: Int
			get() = 1

		fun releaseResolution() {
			resolutionReleased.complete(Unit)
		}

		override suspend fun send(
			mutation: MutationEnvelope<String, TestMutation>
		): TestAck {
			sentIds += mutation.mutationId
			if (mutation.mutationId == "mutation-1") throw TestTerminalFailure()
			return TestAck(mutation.mutationId, mutation.command.value)
		}

		override suspend fun confirm(
			mutation: MutationEnvelope<String, TestMutation>,
			ack: TestAck
		) = Unit

		override fun classifyError(
			mutation: MutationEnvelope<String, TestMutation>,
			throwable: Throwable
		) = MutationFailureKind.Conflict

		override suspend fun resolveFailure(
			mutation: MutationEnvelope<String, TestMutation>,
			throwable: Throwable
		): MutationFailureResolution<String, TestMutation> {
			resolutionStarted.complete(Unit)
			resolutionReleased.await()
			return onResolve(mutation)
		}
	}

	// "mutation-1" is held in its precondition until the test releases it; then it gets a newer revision.
	// Every other mutation keeps the precondition it has. The ids sent are recorded.
	private class GatedPreconditionSpec : MutationSyncSpec<String, TestMutation, TestAck> {
		val preconditionStarted = CompletableDeferred<Unit>()
		private val preconditionReleased = CompletableDeferred<Unit>()
		val sentIds = mutableListOf<String>()

		fun releasePrecondition() {
			preconditionReleased.complete(Unit)
		}

		override suspend fun currentPrecondition(
			mutation: MutationEnvelope<String, TestMutation>
		): MutationPrecondition {
			if (mutation.mutationId != "mutation-1") return mutation.precondition

			preconditionStarted.complete(Unit)
			preconditionReleased.await()
			return MutationPrecondition.Revision(2L)
		}

		override suspend fun send(
			mutation: MutationEnvelope<String, TestMutation>
		): TestAck {
			sentIds += mutation.mutationId
			return TestAck(mutation.mutationId, mutation.command.value)
		}

		override suspend fun confirm(
			mutation: MutationEnvelope<String, TestMutation>,
			ack: TestAck
		) = Unit
	}

	// A spec whose send always fails, and whose failure is classified and resolved as the test says.
	private class FailingSpec(
		private val kind: MutationFailureKind,
		private val exhaustedLimit: Int = Int.MAX_VALUE,
		private val onResolve: (MutationEnvelope<String, TestMutation>) -> MutationFailureResolution<String, TestMutation>
	) : MutationSyncSpec<String, TestMutation, TestAck> {
		override val maxExhaustedExecutions: Int
			get() = exhaustedLimit

		override suspend fun send(
			mutation: MutationEnvelope<String, TestMutation>
		): TestAck = throw TestTerminalFailure()

		override suspend fun confirm(
			mutation: MutationEnvelope<String, TestMutation>,
			ack: TestAck
		) = Unit

		override fun classifyError(
			mutation: MutationEnvelope<String, TestMutation>,
			throwable: Throwable
		) = kind

		override suspend fun resolveFailure(
			mutation: MutationEnvelope<String, TestMutation>,
			throwable: Throwable
		): MutationFailureResolution<String, TestMutation> = onResolve(mutation)
	}
}
