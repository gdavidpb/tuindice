package com.gdavidpb.tuindice.persistence.domain.mutation

import com.gdavidpb.tuindice.base.domain.model.mutation.OutboxMutation
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

class StoreBackedMutationEngineTest {
	@Test
	fun submit_whenReplacedBySameReplaceKey_keepsLatestPendingAndSuppressesStaleAck() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		val firstSendStarted = CompletableDeferred<Unit>()
		val releaseFirstAck = CompletableDeferred<Unit>()
		val confirmedValues = mutableListOf<Int>()
		val sentValues = mutableListOf<Int>()
		val syncSpec = object : MutationSyncSpec<String, TestMutation, Unit, Unit, TestAck> {
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
	fun submit_whenRebaseChangesRevision_retriesWithUpdatedPrecondition() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		val sentRevisions = mutableListOf<Long?>()
		val confirmedValues = mutableListOf<Int>()
		var attempts = 0
		val syncSpec = object : MutationSyncSpec<String, TestMutation, Unit, Unit, TestAck> {
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
		val syncSpec = object : MutationSyncSpec<String, TestMutation, Unit, Unit, TestAck> {
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

		val failedMutation = store.getPendingMutations("record").single()
		assertEquals(PendingMutationStatus.Failed, failedMutation.status)
		assertIs<String>(failedMutation.lastError)
	}

	@Test
	fun submit_whenPreconditionIsNone_supportsSimpleWriteFlow() = runTest {
		val store = InMemoryMutationEnvelopeStore<String, TestMutation>()
		val engine = createEngine(store, this)
		val seenPreconditions = mutableListOf<MutationPrecondition>()
		val confirmedValues = mutableListOf<Int>()
		val syncSpec = object : MutationSyncSpec<String, TestMutation, Unit, Unit, TestAck> {
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
}

private fun createEngine(
	store: MutationEnvelopeStore<String, TestMutation>,
	coroutineScope: kotlinx.coroutines.CoroutineScope
) = StoreBackedMutationEngine<String, TestMutation, Unit, Unit, TestAck>(
	storeId = "test",
	outboxStore = store,
	coroutineScope = coroutineScope
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

	override fun observePendingMutations(
		scopeKey: ScopeKey
	): Flow<List<MutationEnvelope<ScopeKey, Command>>> {
		return state.map { mutations ->
			mutations.filter { mutation -> mutation.scopeKey == scopeKey }
		}
	}

	override suspend fun getPendingMutations(
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
		state.value = state.value
			.filterNot { pending -> pending.mutationId == mutation.mutationId }
			.plus(mutation)
	}

	override suspend fun deletePendingMutation(
		scopeKey: ScopeKey,
		mutationId: String
	) {
		state.value = state.value.filterNot { mutation ->
			mutation.scopeKey == scopeKey && mutation.mutationId == mutationId
		}
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
