package com.gdavidpb.tuindice.persistence.domain.mutation

import com.gdavidpb.tuindice.base.domain.model.mutation.OutboxMutation
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StoreBackedMutationEngineTest {
	@Test
	fun submit_whenReplacedBySameReplaceKey_keepsLatestPendingAndSuppressesStaleAck() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		val firstSendStarted = CompletableDeferred<Unit>()
		val releaseFirstAck = CompletableDeferred<Unit>()
		val confirmedValues = mutableListOf<Int>()
		val sentValues = mutableListOf<Int>()
		val syncSpec = object : MutationSyncSpec<String, TestMutation, TestAck> {
			override suspend fun send(
				mutation: MutationEnvelope<String, TestMutation>
			): TestAck {
				sentValues += mutation.command.value
				if (mutation.command.value == 10) {
					firstSendStarted.complete(Unit)
					releaseFirstAck.await()
				}
				return TestAck(mutation.mutationId, mutation.command.value)
			}

			override suspend fun confirm(
				mutation: MutationEnvelope<String, TestMutation>,
				ack: TestAck
			) {
				confirmedValues += ack.value
			}
		}

		val firstMutation = testMutationEnvelope(
			mutationId = "mutation-1",
			value = 10,
			replaceKey = "subject:1"
		)
		val firstVersion = engine.beginMutation(replaceKey = firstMutation.replaceKey)
		engine.rememberMutationVersion(firstMutation.mutationId, firstVersion)
		val firstJob = launch {
			engine.submit(firstMutation, syncSpec)
		}

		firstSendStarted.await()
		val secondMutation = testMutationEnvelope(
			mutationId = "mutation-2",
			value = 20,
			replaceKey = "subject:1"
		)
		val secondVersion = engine.beginMutation(replaceKey = secondMutation.replaceKey)
		engine.rememberMutationVersion(secondMutation.mutationId, secondVersion)
		val secondJob = launch {
			engine.submit(secondMutation, syncSpec)
		}
		yield()

		assertEquals(
			listOf(secondMutation),
			store.getPendingMutations("record")
		)

		releaseFirstAck.complete(Unit)
		firstJob.join()
		secondJob.join()

		assertEquals(listOf(10, 20), sentValues)
		assertEquals(listOf(20), confirmedValues)
		assertEquals(emptyList(), store.getPendingMutations("record"))
	}

	@Test
	fun submit_whenReplacedBySameReplaceKey_suppressesStaleFailure() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		val firstSendStarted = CompletableDeferred<Unit>()
		val releaseFirstFailure = CompletableDeferred<Unit>()
		val confirmedValues = mutableListOf<Int>()
		val sentValues = mutableListOf<Int>()
		val syncSpec = object : MutationSyncSpec<String, TestMutation, TestAck> {
			override suspend fun send(
				mutation: MutationEnvelope<String, TestMutation>
			): TestAck {
				sentValues += mutation.command.value
				if (mutation.command.value == 10) {
					firstSendStarted.complete(Unit)
					releaseFirstFailure.await()
					throw TestTerminalFailure()
				}
				return TestAck(mutation.mutationId, mutation.command.value)
			}

			override suspend fun confirm(
				mutation: MutationEnvelope<String, TestMutation>,
				ack: TestAck
			) {
				confirmedValues += ack.value
			}
		}

		val firstMutation = testMutationEnvelope(
			mutationId = "mutation-1",
			value = 10,
			replaceKey = "subject:1"
		)
		val firstVersion = engine.beginMutation(replaceKey = firstMutation.replaceKey)
		engine.rememberMutationVersion(firstMutation.mutationId, firstVersion)
		val firstJob = launch {
			engine.submit(firstMutation, syncSpec)
		}

		firstSendStarted.await()
		val secondMutation = testMutationEnvelope(
			mutationId = "mutation-2",
			value = 20,
			replaceKey = "subject:1"
		)
		val secondVersion = engine.beginMutation(replaceKey = secondMutation.replaceKey)
		engine.rememberMutationVersion(secondMutation.mutationId, secondVersion)
		val secondJob = launch {
			engine.submit(secondMutation, syncSpec)
		}
		yield()

		releaseFirstFailure.complete(Unit)
		firstJob.join()
		secondJob.join()

		assertEquals(listOf(10, 20), sentValues)
		assertEquals(listOf(20), confirmedValues)
		assertEquals(emptyList(), store.getPendingMutations("record"))
	}

	@Test
	fun submit_whenRebaseChangesRevision_retriesWithUpdatedPrecondition() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		val sentRevisions = mutableListOf<Long?>()
		val confirmedValues = mutableListOf<Int>()
		var attempts = 0
		val syncSpec = object : MutationSyncSpec<String, TestMutation, TestAck> {
			override val maxRebaseAttempts: Int = 3

			override suspend fun send(
				mutation: MutationEnvelope<String, TestMutation>
			): TestAck {
				attempts += 1
				sentRevisions += mutation.expectedRevision
				if (attempts == 1) throw TestPreconditionFailure()
				return TestAck(mutation.mutationId, mutation.command.value)
			}

			override suspend fun confirm(
				mutation: MutationEnvelope<String, TestMutation>,
				ack: TestAck
			) {
				confirmedValues += ack.value
			}

			override suspend fun resolveFailure(
				mutation: MutationEnvelope<String, TestMutation>,
				throwable: Throwable
			): MutationFailureResolution<String, TestMutation> {
				return MutationFailureResolution.Retry(
					mutation.copy(precondition = MutationPrecondition.Revision(2L))
				)
			}
		}

		engine.submit(
			mutation = testMutationEnvelope(
				mutationId = "mutation-1",
				value = 20,
				precondition = MutationPrecondition.Revision(1L)
			),
			syncSpec = syncSpec
		)

		assertEquals(listOf<Long?>(1L, 2L), sentRevisions)
		assertEquals(listOf(20), confirmedValues)
		assertEquals(emptyList(), store.getPendingMutations("record"))
	}

	@Test
	fun submit_whenRebaseDoesNotAdvancePrecondition_marksMutationFailed() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		val syncSpec = object : MutationSyncSpec<String, TestMutation, TestAck> {
			override suspend fun send(
				mutation: MutationEnvelope<String, TestMutation>
			): TestAck {
				throw TestPreconditionFailure()
			}

			override suspend fun confirm(
				mutation: MutationEnvelope<String, TestMutation>,
				ack: TestAck
			) = Unit

			override suspend fun resolveFailure(
				mutation: MutationEnvelope<String, TestMutation>,
				throwable: Throwable
			): MutationFailureResolution<String, TestMutation> {
				return MutationFailureResolution.Retry(mutation)
			}
		}

		assertFailsWith<TestPreconditionFailure> {
			engine.submit(
				mutation = testMutationEnvelope(
					mutationId = "mutation-1",
					value = 30,
					precondition = MutationPrecondition.Revision(1L)
				),
				syncSpec = syncSpec
			)
		}

		assertEquals(emptyList(), store.getPendingMutations("record"))
		val failedMutation = requireNotNull(store.getPendingMutation("record", "mutation-1"))
		assertEquals(PendingMutationStatus.Failed, failedMutation.status)
		assertIs<String>(failedMutation.lastError)
	}

	@Test
	fun drain_whenAMutationIsConfirmed_theRowsStillWaitingExpectTheNewRevision() = runTest {
		val store = InMemoryMutationEnvelopeStore(
			listOf(
				testMutationEnvelope("mutation-1", value = 5, precondition = MutationPrecondition.Revision(1L)),
				testMutationEnvelope("mutation-2", value = 6, precondition = MutationPrecondition.Revision(1L)),
				testMutationEnvelope("mutation-3", value = 7, precondition = MutationPrecondition.Revision(1L))
			)
		)
		val engine = createEngine(store, this)
		val sentRevisions = mutableListOf<Long?>()
		val syncSpec = object : MutationSyncSpec<String, TestMutation, TestAck> {
			override fun revisionOf(ack: TestAck): Long = ack.value.toLong()

			override suspend fun send(mutation: MutationEnvelope<String, TestMutation>): TestAck {
				sentRevisions += mutation.expectedRevision
				return TestAck(mutation.mutationId, mutation.command.value)
			}

			override suspend fun confirm(mutation: MutationEnvelope<String, TestMutation>, ack: TestAck) = Unit
		}

		engine.drain(scopeKey = "record", syncSpec = syncSpec)

		// Each row is sent with the revision the one before it confirmed, not the one it was queued with.
		assertEquals(listOf<Long?>(1L, 5L, 6L), sentRevisions)
		assertEquals(emptyList(), store.getPendingMutations("record"))
	}

	@Test
	fun drain_whenARowRebases_theRowsStillWaitingExpectTheRebasedRevision() = runTest {
		val store = InMemoryMutationEnvelopeStore(
			listOf(
				testMutationEnvelope("mutation-1", value = 5, precondition = MutationPrecondition.Revision(1L)),
				testMutationEnvelope("mutation-2", value = 6, precondition = MutationPrecondition.Revision(1L))
			)
		)
		val engine = createEngine(store, this)
		val sentRevisions = mutableListOf<Pair<String, Long?>>()
		val syncSpec = object : MutationSyncSpec<String, TestMutation, TestAck> {
			override val maxRebaseAttempts: Int = 3

			override suspend fun send(mutation: MutationEnvelope<String, TestMutation>): TestAck {
				sentRevisions += mutation.mutationId to mutation.expectedRevision
				if (mutation.mutationId == "mutation-1" && mutation.expectedRevision == 1L) throw TestPreconditionFailure()
				return TestAck(mutation.mutationId, mutation.command.value)
			}

			override suspend fun confirm(mutation: MutationEnvelope<String, TestMutation>, ack: TestAck) = Unit

			override suspend fun resolveFailure(
				mutation: MutationEnvelope<String, TestMutation>,
				throwable: Throwable
			): MutationFailureResolution<String, TestMutation> = MutationFailureResolution.Retry(
				mutation.copy(precondition = MutationPrecondition.Revision(9L))
			)
		}

		engine.drain(scopeKey = "record", syncSpec = syncSpec)

		assertEquals(
			listOf<Pair<String, Long?>>("mutation-1" to 1L, "mutation-1" to 9L, "mutation-2" to 9L),
			sentRevisions
		)
	}

	@Test
	fun execution_readsTheCurrentPreconditionInsideTheLock() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		val sentRevisions = mutableListOf<Long?>()
		val syncSpec = object : MutationSyncSpec<String, TestMutation, TestAck> {
			override suspend fun currentPrecondition(
				mutation: MutationEnvelope<String, TestMutation>
			): MutationPrecondition = MutationPrecondition.Revision(9L)

			override suspend fun send(mutation: MutationEnvelope<String, TestMutation>): TestAck {
				sentRevisions += mutation.expectedRevision
				return TestAck(mutation.mutationId, mutation.command.value)
			}

			override suspend fun confirm(mutation: MutationEnvelope<String, TestMutation>, ack: TestAck) = Unit
		}

		engine.submit(
			mutation = testMutationEnvelope("mutation-1", value = 5, precondition = MutationPrecondition.Revision(1L)),
			syncSpec = syncSpec
		)

		assertEquals(listOf<Long?>(9L), sentRevisions)
	}

	@Test
	fun drain_whenRebasesAreExhaustedTooOften_parksTheRowForGood() = runTest {
		val store = InMemoryMutationEnvelopeStore(
			listOf(testMutationEnvelope("mutation-1", value = 5, precondition = MutationPrecondition.Revision(1L)))
		)
		val engine = createEngine(store, this, failedRetryBackoffMillis = 0L)
		val syncSpec = object : MutationSyncSpec<String, TestMutation, TestAck> {
			override val maxRebaseAttempts: Int = 3
			override val maxExhaustedExecutions: Int = 3

			override suspend fun send(mutation: MutationEnvelope<String, TestMutation>): TestAck =
				throw TestPreconditionFailure()

			override suspend fun confirm(mutation: MutationEnvelope<String, TestMutation>, ack: TestAck) = Unit

			// Never learns anything new: the rebase lands on the same revision, so it is exhausted at once.
			override suspend fun resolveFailure(
				mutation: MutationEnvelope<String, TestMutation>,
				throwable: Throwable
			): MutationFailureResolution<String, TestMutation> = MutationFailureResolution.Retry(mutation)
		}

		val statuses = (1..4).map {
			engine.drain(scopeKey = "record", syncSpec = syncSpec)
			requireNotNull(store.getPendingMutation("record", "mutation-1")).let { row ->
				row.status to row.rebaseCount
			}
		}

		assertEquals(
			listOf(
				PendingMutationStatus.Failed to 1,
				PendingMutationStatus.Failed to 2,
				PendingMutationStatus.FailedTerminal to 3,
				// A terminal row is never requeued, so the count stops there.
				PendingMutationStatus.FailedTerminal to 3
			),
			statuses
		)
	}

	@Test
	fun submit_whenPreconditionIsNone_supportsSimpleWriteFlow() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		val seenPreconditions = mutableListOf<MutationPrecondition>()
		val confirmedValues = mutableListOf<Int>()
		val syncSpec = object : MutationSyncSpec<String, TestMutation, TestAck> {
			override suspend fun send(
				mutation: MutationEnvelope<String, TestMutation>
			): TestAck {
				seenPreconditions += mutation.precondition
				return TestAck(mutation.mutationId, mutation.command.value)
			}

			override suspend fun confirm(
				mutation: MutationEnvelope<String, TestMutation>,
				ack: TestAck
			) {
				confirmedValues += ack.value
			}
		}

		engine.submit(
			mutation = testMutationEnvelope(
				mutationId = "mutation-1",
				value = 40,
				precondition = MutationPrecondition.None
			),
			syncSpec = syncSpec
		)

		assertEquals(listOf<MutationPrecondition>(MutationPrecondition.None), seenPreconditions)
		assertEquals(listOf(40), confirmedValues)
		assertNull(store.getPendingMutation("record", "mutation-1"))
	}

	@Test
	fun submit_whenFailureIsDeferred_keepsMutationPending() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		val syncSpec = object : MutationSyncSpec<String, TestMutation, TestAck> {
			override suspend fun send(
				mutation: MutationEnvelope<String, TestMutation>
			): TestAck {
				throw TestTerminalFailure()
			}

			override suspend fun confirm(
				mutation: MutationEnvelope<String, TestMutation>,
				ack: TestAck
			) = Unit

			override suspend fun resolveFailure(
				mutation: MutationEnvelope<String, TestMutation>,
				throwable: Throwable
			): MutationFailureResolution<String, TestMutation> {
				return MutationFailureResolution.Defer()
			}
		}

		engine.submit(
			mutation = testMutationEnvelope(
				mutationId = "mutation-1",
				value = 45
			),
			syncSpec = syncSpec,
			propagateTerminalErrors = true
		)

		val pendingMutation = requireNotNull(store.getPendingMutation("record", "mutation-1"))
		assertEquals(PendingMutationStatus.Pending, pendingMutation.status)
		assertEquals(listOf(pendingMutation), store.getPendingMutations("record"))
		assertIs<String>(pendingMutation.lastError)
	}

	@Test
	fun submitInBackground_enqueuesImmediately_and_confirmsAfterAck() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		val sendStarted = CompletableDeferred<Unit>()
		val releaseAck = CompletableDeferred<Unit>()
		val confirmedValues = mutableListOf<Int>()
		val mutation = testMutationEnvelope(
			mutationId = "mutation-1",
			value = 50
		)
		val syncSpec = object : MutationSyncSpec<String, TestMutation, TestAck> {
			override suspend fun send(
				mutation: MutationEnvelope<String, TestMutation>
			): TestAck {
				sendStarted.complete(Unit)
				releaseAck.await()
				return TestAck(mutation.mutationId, mutation.command.value)
			}

			override suspend fun confirm(
				mutation: MutationEnvelope<String, TestMutation>,
				ack: TestAck
			) {
				confirmedValues += ack.value
			}
		}

		engine.submitInBackground(
			mutation = mutation,
			syncSpec = syncSpec,
			propagateTerminalErrors = true
		)

		assertEquals(listOf(mutation), store.getPendingMutations("record"))
		assertTrue(confirmedValues.isEmpty())

		sendStarted.await()
		assertEquals(listOf(mutation), store.getPendingMutations("record"))

		releaseAck.complete(Unit)
		yield()

		assertEquals(listOf(50), confirmedValues)
		assertEquals(emptyList(), store.getPendingMutations("record"))
	}

	@Test
	fun terminalFailure_leavesMutationOutOfTheSendQueue_butStillVisible() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		val mutation = testMutationEnvelope(
			mutationId = "mutation-1",
			value = 70
		)
		val syncSpec = object : MutationSyncSpec<String, TestMutation, TestAck> {
			override suspend fun send(
				mutation: MutationEnvelope<String, TestMutation>
			): TestAck = throw TestTerminalFailure()

			override suspend fun confirm(
				mutation: MutationEnvelope<String, TestMutation>,
				ack: TestAck
			) = Unit
		}

		engine.submit(mutation, syncSpec, propagateTerminalErrors = false)

		assertEquals(emptyList(), engine.getPendingMutations("record"))

		val visible = engine.getMutations("record").single()
		assertEquals("mutation-1", visible.mutationId)
		assertEquals(PendingMutationStatus.FailedTerminal, visible.status)
		assertEquals(listOf(visible), engine.observeMutations("record").first())
	}

	@Test
	fun drain_leavesTerminallyFailedMutationsAlone_howeverOldTheyAre() = runTest {
		val terminalMutation = testMutationEnvelope(mutationId = "mutation-1", value = 80)
			.copy(status = PendingMutationStatus.FailedTerminal, updatedAt = 1L, lastError = "terminal")
		val store = InMemoryMutationEnvelopeStore(listOf(terminalMutation))
		val engine = createEngine(store, this)
		val sentValues = mutableListOf<Int>()
		val syncSpec = object : MutationSyncSpec<String, TestMutation, TestAck> {
			override suspend fun send(
				mutation: MutationEnvelope<String, TestMutation>
			): TestAck {
				sentValues += mutation.command.value
				return TestAck(mutation.mutationId, mutation.command.value)
			}

			override suspend fun confirm(
				mutation: MutationEnvelope<String, TestMutation>,
				ack: TestAck
			) = Unit
		}

		engine.drain(scopeKey = "record", syncSpec = syncSpec)

		assertEquals(emptyList(), sentValues)
		assertEquals(
			PendingMutationStatus.FailedTerminal,
			engine.getMutations("record").single().status
		)
	}

	@Test
	fun drain_requeuesFailedMutationsPastTheBackoff_andSendsThem() = runTest {
		val failedMutation = testMutationEnvelope(mutationId = "mutation-1", value = 80)
			.copy(status = PendingMutationStatus.Failed, updatedAt = 1L, lastError = "terminal")
		val store = InMemoryMutationEnvelopeStore(listOf(failedMutation))
		val engine = createEngine(store, this)
		val sentValues = mutableListOf<Int>()
		val syncSpec = object : MutationSyncSpec<String, TestMutation, TestAck> {
			override suspend fun send(
				mutation: MutationEnvelope<String, TestMutation>
			): TestAck {
				sentValues += mutation.command.value
				return TestAck(mutation.mutationId, mutation.command.value)
			}

			override suspend fun confirm(
				mutation: MutationEnvelope<String, TestMutation>,
				ack: TestAck
			) = Unit
		}

		engine.drain(scopeKey = "record", syncSpec = syncSpec)

		assertEquals(listOf(80), sentValues)
		assertEquals(emptyList(), store.getMutations("record"))
	}

	@Test
	fun drain_leavesFailedMutationsInsideTheBackoffUntouched() = runTest {
		val failedMutation = testMutationEnvelope(mutationId = "mutation-1", value = 80)
			.copy(status = PendingMutationStatus.Failed, updatedAt = 1L, lastError = "terminal")
		val store = InMemoryMutationEnvelopeStore(listOf(failedMutation))
		val engine = createEngine(store, this, failedRetryBackoffMillis = Long.MAX_VALUE / 2)
		val sentValues = mutableListOf<Int>()
		val syncSpec = object : MutationSyncSpec<String, TestMutation, TestAck> {
			override suspend fun send(
				mutation: MutationEnvelope<String, TestMutation>
			): TestAck {
				sentValues += mutation.command.value
				return TestAck(mutation.mutationId, mutation.command.value)
			}

			override suspend fun confirm(
				mutation: MutationEnvelope<String, TestMutation>,
				ack: TestAck
			) = Unit
		}

		engine.drain(scopeKey = "record", syncSpec = syncSpec)

		assertTrue(sentValues.isEmpty())
		assertEquals(PendingMutationStatus.Failed, store.getMutations("record").single().status)
	}

	@Test
	fun submit_whenResolveFailureThrows_marksMutationFailed_andPropagatesOriginalError() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		val syncSpec = object : MutationSyncSpec<String, TestMutation, TestAck> {
			override suspend fun send(
				mutation: MutationEnvelope<String, TestMutation>
			): TestAck = throw TestTerminalFailure()

			override suspend fun confirm(
				mutation: MutationEnvelope<String, TestMutation>,
				ack: TestAck
			) = Unit

			override suspend fun resolveFailure(
				mutation: MutationEnvelope<String, TestMutation>,
				throwable: Throwable
			): MutationFailureResolution<String, TestMutation> {
				error("resolver crashed")
			}
		}

		assertFailsWith<TestTerminalFailure> {
			engine.submit(
				mutation = testMutationEnvelope(mutationId = "mutation-1", value = 45),
				syncSpec = syncSpec,
				propagateTerminalErrors = true
			)
		}

		val failedMutation = store.getMutations("record").single()
		assertEquals(PendingMutationStatus.Failed, failedMutation.status)
		assertEquals("resolver crashed", failedMutation.lastError)
		assertEquals(emptyList(), store.getPendingMutations("record"))
	}

	@Test
	fun drain_whenResolveFailureThrows_marksMutationFailed_andKeepsDrainingRemainingMutations() = runTest {
		val poisonMutation = testMutationEnvelope(mutationId = "mutation-1", value = 45)
		val healthyMutation = testMutationEnvelope(mutationId = "mutation-2", value = 46)
		val store = InMemoryMutationEnvelopeStore(listOf(poisonMutation, healthyMutation))
		val engine = createEngine(store, this)
		val sentValues = mutableListOf<Int>()
		val confirmedValues = mutableListOf<Int>()
		val syncSpec = object : MutationSyncSpec<String, TestMutation, TestAck> {
			override suspend fun send(
				mutation: MutationEnvelope<String, TestMutation>
			): TestAck {
				sentValues += mutation.command.value
				if (mutation.command.value == 45) throw TestTerminalFailure()
				return TestAck(mutation.mutationId, mutation.command.value)
			}

			override suspend fun confirm(
				mutation: MutationEnvelope<String, TestMutation>,
				ack: TestAck
			) {
				confirmedValues += ack.value
			}

			override suspend fun resolveFailure(
				mutation: MutationEnvelope<String, TestMutation>,
				throwable: Throwable
			): MutationFailureResolution<String, TestMutation> {
				error("resolver crashed")
			}
		}

		engine.drain(scopeKey = "record", syncSpec = syncSpec)

		assertEquals(listOf(45, 46), sentValues)
		assertEquals(listOf(46), confirmedValues)

		val remainingMutation = store.getMutations("record").single()
		assertEquals("mutation-1", remainingMutation.mutationId)
		assertEquals(PendingMutationStatus.Failed, remainingMutation.status)
	}

	@Test
	fun mutationVersion_advancesOnConfirm_soASnapshotFetchedEarlierIsDiscarded() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		val releaseAck = CompletableDeferred<Unit>()
		val syncSpec = object : MutationSyncSpec<String, TestMutation, TestAck> {
			override suspend fun send(
				mutation: MutationEnvelope<String, TestMutation>
			): TestAck {
				releaseAck.await()
				return TestAck(mutation.mutationId, mutation.command.value)
			}

			override suspend fun confirm(
				mutation: MutationEnvelope<String, TestMutation>,
				ack: TestAck
			) = Unit
		}

		val mutation = testMutationEnvelope(mutationId = "mutation-1", value = 90)
		val version = engine.beginMutation(replaceKey = mutation.replaceKey)
		engine.rememberMutationVersion(mutation.mutationId, version)
		engine.submitInBackground(mutation, syncSpec)

		val snapshotVersion = engine.currentMutationVersion()

		releaseAck.complete(Unit)
		yield()

		assertTrue(snapshotVersion != engine.currentMutationVersion())
	}

	@Test
	fun drain_whenTheRunningExecutionLeavesTheRowFailedTerminal_doesNotSendItAgain() = runTest {
		val outcome = drainBehindAnExecutionThatEnds(
			scope = this,
			resolution = { MutationFailureResolution.Fail() }
		)

		assertEquals(1, outcome.sendCount)
		assertEquals(PendingMutationStatus.FailedTerminal, outcome.storedStatus)
	}

	@Test
	fun drain_whenTheRunningExecutionLeavesTheRowFailed_doesNotSendItAgain() = runTest {
		val outcome = drainBehindAnExecutionThatEnds(
			scope = this,
			// A rebase that changes nothing is exhausted at once: the row is parked as Failed.
			resolution = { mutation -> MutationFailureResolution.Retry(mutation) }
		)

		assertEquals(1, outcome.sendCount)
		assertEquals(PendingMutationStatus.Failed, outcome.storedStatus)
	}

	@Test
	fun drain_whenTheRunningExecutionDefersTheRow_sendsItOnceMore() = runTest {
		val outcome = drainBehindAnExecutionThatEnds(
			scope = this,
			resolution = { MutationFailureResolution.Defer() }
		)

		assertEquals(2, outcome.sendCount)
		assertEquals(null, outcome.storedStatus)
	}

	// The resolution of a failure can download (a 409 re-reads the snapshot) and the session can end
	// meanwhile: the wipe has emptied the outbox, and what the engine decides next belongs to the
	// account that left, so it must not put the row back nor send it again.
	@Test
	fun failureResolution_whenTheSessionEndsWhileItSuspends_doesNotResurrectTheRow() = runTest {
		val resolutions = listOf<(MutationEnvelope<String, TestMutation>) -> MutationFailureResolution<String, TestMutation>>(
			{ MutationFailureResolution.Defer() },
			{ MutationFailureResolution.Fail() },
			{ mutation ->
				MutationFailureResolution.Retry(
					mutation.copy(precondition = MutationPrecondition.Revision(9L))
				)
			}
		)

		resolutions.forEach { resolution ->
			val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
			val engine = createEngine(store, this)
			val resolutionStarted = CompletableDeferred<Unit>()
			val releaseResolution = CompletableDeferred<Unit>()
			var sendCount = 0
			val syncSpec = object : MutationSyncSpec<String, TestMutation, TestAck> {
				override val maxRebaseAttempts: Int = 3

				override suspend fun send(
					mutation: MutationEnvelope<String, TestMutation>
				): TestAck {
					sendCount += 1
					throw TestTerminalFailure()
				}

				override suspend fun confirm(
					mutation: MutationEnvelope<String, TestMutation>,
					ack: TestAck
				) = Unit

				override suspend fun resolveFailure(
					mutation: MutationEnvelope<String, TestMutation>,
					throwable: Throwable
				): MutationFailureResolution<String, TestMutation> {
					resolutionStarted.complete(Unit)
					releaseResolution.await()
					return resolution(mutation)
				}
			}
			val job = launch {
				engine.submit(
					mutation = testMutationEnvelope(mutationId = "mutation-1", value = 10),
					syncSpec = syncSpec,
					propagateTerminalErrors = false
				)
			}

			resolutionStarted.await()
			// What the wipe does while the resolution is downloading.
			store.deletePendingMutation("record", "mutation-1")
			engine.clearSessionMemory()
			releaseResolution.complete(Unit)
			job.join()

			assertEquals(emptyList(), store.getMutations("record"))
			assertEquals(1, sendCount)
		}
	}

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

private class DrainBehindExecutionOutcome(
	val sendCount: Int,
	val storedStatus: PendingMutationStatus?
)

// A row is being sent (its first send is held open); a drain that already saw it Pending queues
// behind the scope's lock; the first send then fails and is resolved with [resolution]. The second
// send, if any, succeeds. Returns how many sends happened and the row's status afterwards.
private suspend fun drainBehindAnExecutionThatEnds(
	scope: TestScope,
	resolution: (MutationEnvelope<String, TestMutation>) -> MutationFailureResolution<String, TestMutation>
): DrainBehindExecutionOutcome {
	val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
	val engine = createEngine(store, scope)
	val firstSendStarted = CompletableDeferred<Unit>()
	val releaseFirstSend = CompletableDeferred<Unit>()
	var sendCount = 0
	val syncSpec = object : MutationSyncSpec<String, TestMutation, TestAck> {
		override val maxRebaseAttempts: Int = 3

		override suspend fun send(
			mutation: MutationEnvelope<String, TestMutation>
		): TestAck {
			sendCount += 1
			if (sendCount == 1) {
				firstSendStarted.complete(Unit)
				releaseFirstSend.await()
				throw TestTerminalFailure()
			}
			return TestAck(mutation.mutationId, mutation.command.value)
		}

		override suspend fun confirm(
			mutation: MutationEnvelope<String, TestMutation>,
			ack: TestAck
		) = Unit

		override suspend fun resolveFailure(
			mutation: MutationEnvelope<String, TestMutation>,
			throwable: Throwable
		): MutationFailureResolution<String, TestMutation> = resolution(mutation)
	}
	val mutation = testMutationEnvelope(mutationId = "mutation-1", value = 10)

	val submitJob = scope.launch {
		engine.submit(mutation, syncSpec, propagateTerminalErrors = false)
	}
	firstSendStarted.await()
	val drainJob = scope.launch { engine.drain(scopeKey = "record", syncSpec = syncSpec) }
	scope.testScheduler.runCurrent()

	releaseFirstSend.complete(Unit)
	submitJob.join()
	drainJob.join()

	return DrainBehindExecutionOutcome(
		sendCount = sendCount,
		storedStatus = engine.getMutations("record").singleOrNull()?.status
	)
}

private fun createEngine(
	store: MutationEnvelopeStore<String, TestMutation>,
	coroutineScope: kotlinx.coroutines.CoroutineScope,
	failedRetryBackoffMillis: Long = DEFAULT_FAILED_RETRY_BACKOFF_MILLIS
) = StoreBackedMutationEngine<String, TestMutation, TestAck>(
	storeId = "test",
	outboxStore = store,
	coroutineScope = coroutineScope,
	failedRetryBackoffMillis = failedRetryBackoffMillis
)

private fun testMutationEnvelope(
	mutationId: String,
	value: Int,
	precondition: MutationPrecondition = MutationPrecondition.None,
	replaceKey: String = "entity:$value"
) = MutationEnvelope(
	mutationId = mutationId,
	scopeKey = "record",
	command = TestMutation(
		entityId = "entity-1",
		value = value,
		replaceKey = replaceKey
	),
	precondition = precondition,
	status = PendingMutationStatus.Pending,
	createdAt = 1L,
	updatedAt = 1L,
	lastError = null,
	replaceKey = replaceKey
)

private class InMemoryMutationEnvelopeStore<ScopeKey : Any, Command : OutboxMutation>(
	initialMutations: List<MutationEnvelope<ScopeKey, Command>> = emptyList()
) : MutationEnvelopeStore<ScopeKey, Command> {
	private val state = MutableStateFlow(initialMutations)

	// Runs after each savePendingMutation: the point a test uses to end the session in the middle of
	// an execution.
	var afterSave: (suspend () -> Unit)? = null

	// Runs before each savePendingMutation: the point a test uses to hold a write open.
	var beforeSave: (suspend () -> Unit)? = null

	override fun observePendingMutations(
		scopeKey: ScopeKey
	): Flow<List<MutationEnvelope<ScopeKey, Command>>> {
		return state.map { mutations ->
			mutations.filter { mutation ->
				mutation.scopeKey == scopeKey && mutation.status == PendingMutationStatus.Pending
			}
		}
	}

	override suspend fun getPendingMutations(
		scopeKey: ScopeKey
	): List<MutationEnvelope<ScopeKey, Command>> {
		return state.value.filter { mutation ->
			mutation.scopeKey == scopeKey && mutation.status == PendingMutationStatus.Pending
		}
	}

	override suspend fun requeueFailedMutations(
		scopeKey: ScopeKey,
		retryableBefore: Long
	): Int {
		var requeued = 0
		state.value = state.value.map { mutation ->
			if (
				mutation.scopeKey == scopeKey &&
				mutation.status == PendingMutationStatus.Failed &&
				mutation.updatedAt <= retryableBefore
			) {
				requeued += 1
				mutation.copy(status = PendingMutationStatus.Pending, lastError = null)
			} else {
				mutation
			}
		}
		return requeued
	}

	override fun observeMutations(
		scopeKey: ScopeKey
	): Flow<List<MutationEnvelope<ScopeKey, Command>>> {
		return state.map { mutations ->
			mutations.filter { mutation -> mutation.scopeKey == scopeKey }
		}
	}

	override suspend fun getMutations(
		scopeKey: ScopeKey
	): List<MutationEnvelope<ScopeKey, Command>> {
		return state.value.filter { mutation -> mutation.scopeKey == scopeKey }
	}

	override suspend fun getPendingMutation(
		scopeKey: ScopeKey,
		mutationId: String
	): MutationEnvelope<ScopeKey, Command>? {
		return state.value.firstOrNull { mutation ->
			mutation.scopeKey == scopeKey && mutation.mutationId == mutationId
		}
	}

	override suspend fun replacePendingMutation(
		mutation: MutationEnvelope<ScopeKey, Command>
	) {
		state.value = state.value
			.filterNot { pending -> pending.replaceKey == mutation.replaceKey }
			.plus(mutation)
	}

	override suspend fun savePendingMutation(
		mutation: MutationEnvelope<ScopeKey, Command>
	) {
		beforeSave?.invoke()
		state.value = state.value
			.filterNot { pending -> pending.mutationId == mutation.mutationId }
			.plus(mutation)
		afterSave?.invoke()
	}

	override suspend fun deletePendingMutation(
		scopeKey: ScopeKey,
		mutationId: String
	) {
		state.value = state.value.filterNot { mutation ->
			mutation.scopeKey == scopeKey && mutation.mutationId == mutationId
		}
	}

	override suspend fun advancePendingRevisions(
		scopeKey: ScopeKey,
		revision: Long
	): Int {
		var advanced = 0
		state.value = state.value.map { mutation ->
			val expected = mutation.expectedRevision
			val isWaiting = mutation.status == PendingMutationStatus.Pending ||
				mutation.status == PendingMutationStatus.Failed
			val isBehind = expected != null && expected < revision

			if (mutation.scopeKey == scopeKey && isWaiting && isBehind) {
				advanced += 1
				mutation.copy(precondition = MutationPrecondition.Revision(revision))
			} else {
				mutation
			}
		}
		return advanced
	}
}

@Serializable
private data class TestMutation(
	override val entityId: String,
	val value: Int,
	override val replaceKey: String
) : OutboxMutation {
	override val entityType: String = "test:set"
}

private data class TestAck(
	val mutationId: String,
	val value: Int
)

private class TestPreconditionFailure : RuntimeException("precondition failed")

private class TestTerminalFailure : RuntimeException("terminal failure")

private class ScriptedSpec(
	private val rebaseLimit: () -> Int = { 0 },
	private val onCurrentPrecondition: suspend (MutationEnvelope<String, TestMutation>) -> MutationPrecondition =
		{ mutation -> mutation.precondition },
	private val onSend: suspend (MutationEnvelope<String, TestMutation>) -> Unit = {},
	private val onConfirm: suspend () -> Unit = {},
	private val onResolve: suspend (
		MutationEnvelope<String, TestMutation>,
		Throwable
	) -> MutationFailureResolution<String, TestMutation> = { _, _ -> MutationFailureResolution.Fail() }
) : MutationSyncSpec<String, TestMutation, TestAck> {
	var sendCount = 0
		private set
	var confirmCount = 0
		private set
	var resolveCount = 0
		private set

	override val maxRebaseAttempts: Int
		get() = rebaseLimit()

	override suspend fun currentPrecondition(
		mutation: MutationEnvelope<String, TestMutation>
	): MutationPrecondition = onCurrentPrecondition(mutation)

	override suspend fun send(mutation: MutationEnvelope<String, TestMutation>): TestAck {
		sendCount += 1
		onSend(mutation)
		return TestAck(mutation.mutationId, mutation.command.value)
	}

	override suspend fun confirm(mutation: MutationEnvelope<String, TestMutation>, ack: TestAck) {
		confirmCount += 1
		onConfirm()
	}

	override suspend fun resolveFailure(
		mutation: MutationEnvelope<String, TestMutation>,
		throwable: Throwable
	): MutationFailureResolution<String, TestMutation> {
		resolveCount += 1
		return onResolve(mutation, throwable)
	}
}
