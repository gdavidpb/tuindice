package com.gdavidpb.tuindice.persistence.domain.mutation

import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcome
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcomeKind
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals

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
