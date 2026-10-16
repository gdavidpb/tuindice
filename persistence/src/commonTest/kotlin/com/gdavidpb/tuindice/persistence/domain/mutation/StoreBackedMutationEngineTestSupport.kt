package com.gdavidpb.tuindice.persistence.domain.mutation

import com.gdavidpb.tuindice.base.domain.model.mutation.OutboxMutation
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

internal fun createEngine(
	store: MutationEnvelopeStore<String, TestMutation>,
	coroutineScope: kotlinx.coroutines.CoroutineScope,
	failedRetryBackoffMillis: Long = DEFAULT_FAILED_RETRY_BACKOFF_MILLIS
) = StoreBackedMutationEngine<String, TestMutation, TestAck>(
	storeId = "test",
	outboxStore = store,
	coroutineScope = coroutineScope,
	failedRetryBackoffMillis = failedRetryBackoffMillis
)

internal fun testMutationEnvelope(
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

internal class InMemoryMutationEnvelopeStore<ScopeKey : Any, Command : OutboxMutation>(
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
internal data class TestMutation(
	override val entityId: String,
	val value: Int,
	override val replaceKey: String
) : OutboxMutation {
	override val entityType: String = "test:set"
}

internal data class TestAck(
	val mutationId: String,
	val value: Int
)

internal class TestPreconditionFailure : RuntimeException("precondition failed")

internal class TestTerminalFailure : RuntimeException("terminal failure")

internal class ScriptedSpec(
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
