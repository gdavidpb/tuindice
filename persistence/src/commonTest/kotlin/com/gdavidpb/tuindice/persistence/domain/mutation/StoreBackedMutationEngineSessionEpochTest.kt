package com.gdavidpb.tuindice.persistence.domain.mutation

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StoreBackedMutationEngineSessionEpochTest {
	// The guards of the session epoch, one per wait a sign-out can cross. Each test ends the session
	// (clearSessionMemory) at exactly that point and leaves the row where it was: the guard under
	// test is then the only thing between the engine and the account that left. What must not happen
	// afterwards is a row written back or an answer sent or confirmed on behalf of that account.
	@Test
	fun sessionEpoch_whenTheSessionEndsInCurrentPrecondition_aligningNeitherSavesNorSends() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		val started = CompletableDeferred<Unit>()
		val release = CompletableDeferred<Unit>()
		val spec = ScriptedSpec(
			onCurrentPrecondition = {
				started.complete(Unit)
				release.await()
				MutationPrecondition.Revision(7L)
			}
		)
		val mutation = testMutationEnvelope(mutationId = "mutation-1", value = 10)
		val job = launch { engine.submit(mutation, spec, propagateTerminalErrors = false) }

		started.await()
		engine.clearSessionMemory()
		release.complete(Unit)
		job.join()

		assertEquals(0, spec.sendCount)
		assertEquals(listOf(mutation), store.getMutations("record"), "the aligned row was not saved")
	}

	// The session cannot end in the middle of a local write of the queue: the end waits for it, and the
	// wipe that follows removes what it wrote. One test per kind of write that follows an epoch check.
	@Test
	fun sessionEpoch_whenTheAlignedRowIsBeingSaved_theEndOfTheSessionWaitsForTheSave() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		val saveStarted = CompletableDeferred<Unit>()
		val releaseSave = CompletableDeferred<Unit>()
		val spec = ScriptedSpec(
			onCurrentPrecondition = {
				store.beforeSave = {
					store.beforeSave = null
					saveStarted.complete(Unit)
					releaseSave.await()
				}
				MutationPrecondition.Revision(7L)
			}
		)
		val job = launch {
			engine.submit(
				mutation = testMutationEnvelope(mutationId = "mutation-1", value = 10),
				syncSpec = spec,
				propagateTerminalErrors = false
			)
		}

		saveStarted.await()
		var ended = false
		val ending = launch {
			engine.clearSessionMemory()
			ended = true
		}
		testScheduler.runCurrent()
		assertFalse(ended, "the session does not end in the middle of the save")

		releaseSave.complete(Unit)
		job.join()
		ending.join()

		assertTrue(ended)
		assertEquals(
			MutationPrecondition.Revision(7L),
			store.getMutations("record").single().precondition,
			"the save ended before the session did"
		)
	}

	@Test
	fun sessionEpoch_whenTheAckIsBeingConfirmed_theEndOfTheSessionWaitsForTheConfirmation() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		val confirmStarted = CompletableDeferred<Unit>()
		val releaseConfirm = CompletableDeferred<Unit>()
		val spec = ScriptedSpec(
			onConfirm = {
				confirmStarted.complete(Unit)
				releaseConfirm.await()
			}
		)
		val job = launch {
			engine.submit(
				mutation = testMutationEnvelope(mutationId = "mutation-1", value = 10),
				syncSpec = spec,
				propagateTerminalErrors = false
			)
		}

		confirmStarted.await()
		var ended = false
		val ending = launch {
			engine.clearSessionMemory()
			ended = true
		}
		testScheduler.runCurrent()
		assertFalse(ended, "the session does not end in the middle of the confirmation")

		releaseConfirm.complete(Unit)
		job.join()
		ending.join()

		assertTrue(ended)
		assertEquals(emptyList(), store.getMutations("record"), "the row was retired before the session ended")
	}

	@Test
	fun sessionEpoch_whenAResolutionIsBeingSaved_theEndOfTheSessionWaitsForIt() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		val saveStarted = CompletableDeferred<Unit>()
		val releaseSave = CompletableDeferred<Unit>()
		val spec = ScriptedSpec(
			onSend = { throw TestTerminalFailure() },
			onResolve = { _, _ ->
				store.beforeSave = {
					store.beforeSave = null
					saveStarted.complete(Unit)
					releaseSave.await()
				}
				MutationFailureResolution.Defer()
			}
		)
		val job = launch {
			engine.submit(
				mutation = testMutationEnvelope(mutationId = "mutation-1", value = 10),
				syncSpec = spec,
				propagateTerminalErrors = false
			)
		}

		saveStarted.await()
		var ended = false
		val ending = launch {
			engine.clearSessionMemory()
			ended = true
		}
		testScheduler.runCurrent()
		assertFalse(ended, "the session does not end in the middle of the verdict's save")

		releaseSave.complete(Unit)
		job.join()
		ending.join()

		assertTrue(ended)
	}

	@Test
	fun sessionEpoch_theGateIsNotHeldDuringARequest() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		val sendStarted = CompletableDeferred<Unit>()
		val releaseSend = CompletableDeferred<Unit>()
		val spec = ScriptedSpec(
			onSend = {
				sendStarted.complete(Unit)
				releaseSend.await()
			}
		)
		val job = launch {
			engine.submit(
				mutation = testMutationEnvelope(mutationId = "mutation-1", value = 10),
				syncSpec = spec,
				propagateTerminalErrors = false
			)
		}

		sendStarted.await()
		// Returns at once: the send in flight does not hold what the end of the session needs.
		engine.clearSessionMemory()
		releaseSend.complete(Unit)
		job.join()
	}

	// What a 401 does: the request itself ends the session (the wipe of its rejection tells every holder,
	// this engine included) while it is still being sent. It can only finish if the gate is not held
	// during the send; held, the request would wait for a gate that waits for the request.
	@Test
	fun sessionEpoch_aRejectionInsideTheSendCanEndTheSession_becauseTheGateIsNotHeldDuringSend() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		val spec = ScriptedSpec(
			onSend = {
				engine.clearSessionMemory()
				throw TestTerminalFailure()
			}
		)
		val mutation = testMutationEnvelope(mutationId = "mutation-1", value = 10)

		engine.submit(mutation, spec, propagateTerminalErrors = false)

		assertEquals(1, spec.sendCount)
		assertEquals(0, spec.resolveCount, "the rejection belongs to the account that left")
		assertEquals(listOf(mutation), store.getMutations("record"))
	}

	@Test
	fun sessionEpoch_whenTheSessionEndsWhileSending_theAckIsNotConfirmed() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		val started = CompletableDeferred<Unit>()
		val release = CompletableDeferred<Unit>()
		val spec = ScriptedSpec(
			onSend = {
				started.complete(Unit)
				release.await()
			}
		)
		val mutation = testMutationEnvelope(mutationId = "mutation-1", value = 10)
		val job = launch { engine.submit(mutation, spec, propagateTerminalErrors = false) }

		started.await()
		engine.clearSessionMemory()
		release.complete(Unit)
		job.join()

		assertEquals(1, spec.sendCount)
		assertEquals(0, spec.confirmCount, "the answer belongs to the account that left")
		assertEquals(listOf(mutation), store.getMutations("record"), "and the row is not retired")
	}

	@Test
	fun sessionEpoch_whenTheSessionEndsWhileSendingAndTheSendFails_theFailureIsNotResolved() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		val started = CompletableDeferred<Unit>()
		val release = CompletableDeferred<Unit>()
		val spec = ScriptedSpec(
			onSend = {
				started.complete(Unit)
				release.await()
				throw TestTerminalFailure()
			}
		)
		val mutation = testMutationEnvelope(mutationId = "mutation-1", value = 10)
		val job = launch { engine.submit(mutation, spec, propagateTerminalErrors = false) }

		started.await()
		engine.clearSessionMemory()
		release.complete(Unit)
		job.join()

		assertEquals(0, spec.resolveCount, "no verdict is asked for a failure of the account that left")
		assertEquals(listOf(mutation), store.getMutations("record"), "and none is saved")
	}

	@Test
	fun sessionEpoch_whenTheSessionEndsWhileTheResolverFailsToDecide_theRowIsNotMarkedFailed() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		val started = CompletableDeferred<Unit>()
		val release = CompletableDeferred<Unit>()
		val spec = ScriptedSpec(
			onSend = { throw TestTerminalFailure() },
			onResolve = { _, _ ->
				started.complete(Unit)
				release.await()
				error("the resolver cannot decide")
			}
		)
		val mutation = testMutationEnvelope(mutationId = "mutation-1", value = 10)
		val job = launch { engine.submit(mutation, spec, propagateTerminalErrors = false) }

		started.await()
		engine.clearSessionMemory()
		release.complete(Unit)
		job.join()

		assertEquals(listOf(mutation), store.getMutations("record"), "the failed mark is not written back")
	}

	@Test
	fun sessionEpoch_whenTheSessionEndsRightBeforeTheRebaseIsSaved_theRebasedRowIsNotSavedNorSent() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		var ended = false
		val spec = ScriptedSpec(
			// Read once the verdict is a Retry and before the rebased row is saved: the point where
			// no suspension separates the epoch check of the resolution from the save.
			rebaseLimit = {
				if (!ended) {
					ended = true
					launch(start = CoroutineStart.UNDISPATCHED) { engine.clearSessionMemory() }
				}
				3
			},
			onSend = { throw TestTerminalFailure() },
			onResolve = { mutation, _ ->
				MutationFailureResolution.Retry(
					mutation.copy(precondition = MutationPrecondition.Revision(9L))
				)
			}
		)
		val mutation = testMutationEnvelope(mutationId = "mutation-1", value = 10)

		engine.submit(mutation, spec, propagateTerminalErrors = false)

		assertTrue(ended)
		assertEquals(1, spec.sendCount, "no second attempt for the account that left")
		assertEquals(listOf(mutation), store.getMutations("record"), "the rebased row was not saved")
	}
}
