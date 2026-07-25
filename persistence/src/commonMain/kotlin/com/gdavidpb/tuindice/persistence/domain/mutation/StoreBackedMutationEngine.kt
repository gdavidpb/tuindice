package com.gdavidpb.tuindice.persistence.domain.mutation

import com.gdavidpb.tuindice.base.domain.model.mutation.OutboxMutation
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.*

const val DEFAULT_FAILED_RETRY_BACKOFF_MILLIS: Long = 5 * 60 * 1000

@OptIn(ExperimentalStoreApi::class)
class StoreBackedMutationEngine<ScopeKey : Any, Command : OutboxMutation, Ack : Any>(
	private val storeId: String,
	private val outboxStore: MutationEnvelopeStore<ScopeKey, Command>,
	private val coroutineScope: CoroutineScope,
	private val failedRetryBackoffMillis: Long = DEFAULT_FAILED_RETRY_BACKOFF_MILLIS
) {
	private val versionMutex = Mutex()
	private val runtimeBookkeeperMutex = Mutex()
	private val executionMutexesMutex = Mutex()
	private val latestMutationVersionByReplaceKey = mutableMapOf<String, Long>()
	private val mutationVersionById = mutableMapOf<String, Long>()
	private val runtimeBookkeeper = mutableMapOf<ScopeKey, Long>()
	private val executionMutexes = mutableMapOf<ScopeKey, Mutex>()
	private val runtimeState =
		MutableStateFlow<Map<ScopeKey, MutationExecution<ScopeKey, Command, Ack>>>(
			emptyMap()
		)

	private var latestMutationVersion = 0L

	private val runtimeStore: MutableStore<ScopeKey, MutationExecution<ScopeKey, Command, Ack>> =
		MutableStoreBuilder.from<
				ScopeKey,
				MutationExecution<ScopeKey, Command, Ack>,
				MutationExecution<ScopeKey, Command, Ack>,
				MutationExecution<ScopeKey, Command, Ack>
				>(
			fetcher = Fetcher.of(name = "$storeId-runtime") { _: ScopeKey ->
				MutationExecution.Idle()
			},
			sourceOfTruth = object : SourceOfTruth<
					ScopeKey,
					MutationExecution<ScopeKey, Command, Ack>,
					MutationExecution<ScopeKey, Command, Ack>
					> {
				override fun reader(
					key: ScopeKey
				) = runtimeState.map { executions ->
					executions[key] ?: MutationExecution.Idle()
				}

				override suspend fun write(
					key: ScopeKey,
					value: MutationExecution<ScopeKey, Command, Ack>
				) {
					runtimeState.value = runtimeState.value.toMutableMap()
						.apply {
							put(key, value)
						}
						.toMap()
				}

				override suspend fun delete(key: ScopeKey) {
					runtimeState.value = runtimeState.value.toMutableMap()
						.apply { remove(key) }
						.toMap()
				}

				override suspend fun deleteAll() {
					runtimeState.value = emptyMap()
				}
			},
			converter = object : Converter<
					MutationExecution<ScopeKey, Command, Ack>,
					MutationExecution<ScopeKey, Command, Ack>,
					MutationExecution<ScopeKey, Command, Ack>
					> {
				override fun fromNetworkToLocal(
					network: MutationExecution<ScopeKey, Command, Ack>
				): MutationExecution<ScopeKey, Command, Ack> = network

				override fun fromOutputToLocal(
					output: MutationExecution<ScopeKey, Command, Ack>
				): MutationExecution<ScopeKey, Command, Ack> = output
			}
		)
			.scope(coroutineScope)
			.disableCache()
			.build(
				updater = Updater.by(
					post = { _, execution ->
						when (execution) {
							is MutationExecution.Execute -> processExecution(execution)
							is MutationExecution.Idle -> UpdaterResult.Success.Untyped(Unit)
						}
					},
					onCompletion = OnUpdaterCompletion(
						onSuccess = { _ -> },
						onFailure = { _ -> }
					)
				),
				bookkeeper = Bookkeeper.by(
					getLastFailedSync = { key: ScopeKey ->
						runtimeBookkeeperMutex.withLock { runtimeBookkeeper[key] }
					},
					setLastFailedSync = { key: ScopeKey, lastFailedSync: Long ->
						runtimeBookkeeperMutex.withLock {
							runtimeBookkeeper[key] = lastFailedSync
						}
						true
					},
					clear = { key: ScopeKey ->
						runtimeBookkeeperMutex.withLock {
							runtimeBookkeeper.remove(key)
						}
						true
					},
					clearAll = {
						runtimeBookkeeperMutex.withLock {
							runtimeBookkeeper.clear()
						}
						true
					}
				)
			)

	fun observePendingMutations(
		scopeKey: ScopeKey
	) = outboxStore.observePendingMutations(scopeKey)

	suspend fun getPendingMutations(
		scopeKey: ScopeKey
	): List<MutationEnvelope<ScopeKey, Command>> {
		return outboxStore.getPendingMutations(scopeKey)
	}

	fun observeMutations(
		scopeKey: ScopeKey
	) = outboxStore.observeMutations(scopeKey)

	suspend fun getMutations(
		scopeKey: ScopeKey
	): List<MutationEnvelope<ScopeKey, Command>> {
		return outboxStore.getMutations(scopeKey)
	}

	suspend fun getPendingMutation(
		scopeKey: ScopeKey,
		mutationId: String
	): MutationEnvelope<ScopeKey, Command>? {
		return outboxStore.getPendingMutation(scopeKey, mutationId)
	}

	suspend fun beginMutation(
		replaceKey: String? = null
	): Long {
		return versionMutex.withLock {
			latestMutationVersion += 1
			replaceKey?.let { key ->
				latestMutationVersionByReplaceKey[key] = latestMutationVersion
			}
			latestMutationVersion
		}
	}

	suspend fun currentMutationVersion(): Long {
		return versionMutex.withLock { latestMutationVersion }
	}

	suspend fun rememberMutationVersion(
		mutationId: String,
		version: Long
	) {
		versionMutex.withLock {
			mutationVersionById[mutationId] = version
		}
	}

	suspend fun submit(
		mutation: MutationEnvelope<ScopeKey, Command>,
		syncSpec: MutationSyncSpec<ScopeKey, Command, Ack>,
		propagateTerminalErrors: Boolean = true
	) {
		outboxStore.replacePendingMutation(mutation)
		executeMutation(
			MutationExecution.Execute(
				mutation = mutation,
				syncSpec = syncSpec,
				propagateTerminalErrors = propagateTerminalErrors
				)
		)
	}

	suspend fun submitInBackground(
		mutation: MutationEnvelope<ScopeKey, Command>,
		syncSpec: MutationSyncSpec<ScopeKey, Command, Ack>,
		propagateTerminalErrors: Boolean = false
	) {
		outboxStore.replacePendingMutation(mutation)
		coroutineScope.launch {
			executeMutation(
				MutationExecution.Execute(
					mutation = mutation,
					syncSpec = syncSpec,
					propagateTerminalErrors = propagateTerminalErrors
				)
			)
		}
	}

	suspend fun drain(
		scopeKey: ScopeKey,
		syncSpec: MutationSyncSpec<ScopeKey, Command, Ack>,
		propagateTerminalErrors: Boolean = false,
		targetMutationId: String? = null
	) {
		outboxStore.requeueFailedMutations(
			scopeKey = scopeKey,
			retryableBefore = currentTimeMillis() - failedRetryBackoffMillis
		)

		getPendingMutations(scopeKey).forEach { mutation ->
			if (targetMutationId != null && mutation.mutationId != targetMutationId) {
				return@forEach
			}

			executeMutation(
				MutationExecution.Execute(
					mutation = mutation,
					syncSpec = syncSpec,
					propagateTerminalErrors = propagateTerminalErrors
				)
			)
		}
	}

	suspend fun deletePendingMutation(
		scopeKey: ScopeKey,
		mutationId: String
	) {
		val mutation = outboxStore.getPendingMutation(scopeKey, mutationId)

		outboxStore.deletePendingMutation(scopeKey, mutationId)
		mutation?.let { deleted -> forgetMutationVersion(deleted) }
		advanceMutationVersion()
	}

	private suspend fun executeMutation(
		execution: MutationExecution.Execute<ScopeKey, Command, Ack>
	) {
		withScopeExecutionLock(execution.mutation.scopeKey) {
			when (
				val response = runtimeStore.write<Ack>(
					StoreWriteRequest.of(
						execution.mutation.scopeKey,
						execution
					)
				)
			) {
				is StoreWriteResponse.Error.Exception -> throw response.error
				else -> Unit
			}
		}
	}

	private suspend fun processExecution(
		execution: MutationExecution.Execute<ScopeKey, Command, Ack>
	): UpdaterResult {
		var currentMutation = execution.mutation
		var rebaseAttempts = 0

		while (true) {
			val step = attemptMutation(
				execution = execution,
				mutation = currentMutation,
				rebaseAttempts = rebaseAttempts
			)

			if (step !is ExecutionStep.Rebase) return (step as ExecutionStep.Done).result

			rebaseAttempts += 1
			currentMutation = step.mutation
			outboxStore.savePendingMutation(currentMutation)
		}
	}

	private suspend fun attemptMutation(
		execution: MutationExecution.Execute<ScopeKey, Command, Ack>,
		mutation: MutationEnvelope<ScopeKey, Command>,
		rebaseAttempts: Int
	): ExecutionStep<ScopeKey, Command> {
		if (!shouldApplyMutation(mutation)) return suppressMutation(mutation)

		return try {
			val ack = execution.syncSpec.send(mutation)

			if (shouldApplyMutation(mutation)) {
				confirmMutation(execution.syncSpec, mutation, ack)
				ExecutionStep.Done(UpdaterResult.Success.Typed(ack))
			} else {
				suppressMutation(mutation)
			}
		} catch (throwable: Throwable) {
			if (throwable is CancellationException) throw throwable

			if (shouldApplyMutation(mutation)) {
				resolveExecutionFailure(execution, mutation, throwable, rebaseAttempts)
			} else {
				suppressMutation(mutation)
			}
		}
	}

	private suspend fun suppressMutation(
		mutation: MutationEnvelope<ScopeKey, Command>
	): ExecutionStep<ScopeKey, Command> {
		forgetMutationVersion(mutation)

		return ExecutionStep.Done(UpdaterResult.Success.Untyped(Unit))
	}

	private suspend fun confirmMutation(
		syncSpec: MutationSyncSpec<ScopeKey, Command, Ack>,
		mutation: MutationEnvelope<ScopeKey, Command>,
		ack: Ack
	) {
		suspend fun retire() {
			outboxStore.deletePendingMutation(
				scopeKey = mutation.scopeKey,
				mutationId = mutation.mutationId
			)
			forgetMutationVersion(mutation)
		}

		if (syncSpec.deletePendingBeforeConfirm(mutation)) {
			retire()
			syncSpec.confirm(mutation, ack)
		} else {
			syncSpec.confirm(mutation, ack)
			retire()
		}

		advanceMutationVersion()
	}

	private suspend fun resolveExecutionFailure(
		execution: MutationExecution.Execute<ScopeKey, Command, Ack>,
		mutation: MutationEnvelope<ScopeKey, Command>,
		throwable: Throwable,
		rebaseAttempts: Int
	): ExecutionStep<ScopeKey, Command> {
		return when (val resolution = execution.syncSpec.resolveFailure(mutation, throwable)) {
			is MutationFailureResolution.Defer -> {
				outboxStore.savePendingMutation(
					mutation.retried(lastError = resolution.lastError ?: throwable.message)
				)

				ExecutionStep.Done(UpdaterResult.Success.Untyped(Unit))
			}

			is MutationFailureResolution.Drop -> {
				outboxStore.deletePendingMutation(
					scopeKey = mutation.scopeKey,
					mutationId = mutation.mutationId
				)
				forgetMutationVersion(mutation)
				advanceMutationVersion()

				terminalStep(throwable, resolution.propagate && execution.propagateTerminalErrors)
			}

			is MutationFailureResolution.Fail -> {
				outboxStore.savePendingMutation(mutation.failedTerminally(throwable.message))

				terminalStep(throwable, resolution.propagate && execution.propagateTerminalErrors)
			}

			is MutationFailureResolution.Retry -> resolveRebase(
				execution = execution,
				mutation = mutation,
				rebased = resolution.mutation,
				throwable = throwable,
				rebaseAttempts = rebaseAttempts
			)
		}
	}

	private suspend fun resolveRebase(
		execution: MutationExecution.Execute<ScopeKey, Command, Ack>,
		mutation: MutationEnvelope<ScopeKey, Command>,
		rebased: MutationEnvelope<ScopeKey, Command>,
		throwable: Throwable,
		rebaseAttempts: Int
	): ExecutionStep<ScopeKey, Command> {
		val rebasedMutation = rebased.retried(lastError = null)
		val exhausted = rebasedMutation.precondition == mutation.precondition ||
			rebaseAttempts >= execution.syncSpec.maxRebaseAttempts

		if (!exhausted) return ExecutionStep.Rebase(rebasedMutation)

		outboxStore.savePendingMutation(rebasedMutation.failed(throwable.message))

		return terminalStep(throwable, execution.propagateTerminalErrors)
	}

	private fun terminalStep(
		throwable: Throwable,
		propagate: Boolean
	): ExecutionStep<ScopeKey, Command> {
		val result = if (propagate) {
			UpdaterResult.Error.Exception(throwable)
		} else {
			UpdaterResult.Success.Untyped(Unit)
		}

		return ExecutionStep.Done(result)
	}

	private fun MutationEnvelope<ScopeKey, Command>.retried(
		lastError: String?
	) = copy(
		status = PendingMutationStatus.Pending,
		updatedAt = currentTimeMillis(),
		lastError = lastError
	)

	private fun MutationEnvelope<ScopeKey, Command>.failed(
		lastError: String?
	) = copy(
		status = PendingMutationStatus.Failed,
		updatedAt = currentTimeMillis(),
		lastError = lastError
	)

	private fun MutationEnvelope<ScopeKey, Command>.failedTerminally(
		lastError: String?
	) = copy(
		status = PendingMutationStatus.FailedTerminal,
		updatedAt = currentTimeMillis(),
		lastError = lastError
	)

	private suspend fun shouldApplyMutation(
		mutation: MutationEnvelope<ScopeKey, Command>
	): Boolean {
		if (outboxStore.getPendingMutation(mutation.scopeKey, mutation.mutationId) == null) {
			return false
		}

		return versionMutex.withLock {
			val mutationVersion = mutationVersionById[mutation.mutationId]
				?: return@withLock true
			latestMutationVersionByReplaceKey[mutation.replaceKey] == mutationVersion
		}
	}

	private suspend fun forgetMutationVersion(
		mutation: MutationEnvelope<ScopeKey, Command>
	) {
		versionMutex.withLock {
			val forgottenVersion = mutationVersionById.remove(mutation.mutationId)

			if (latestMutationVersionByReplaceKey[mutation.replaceKey] == forgottenVersion) {
				latestMutationVersionByReplaceKey.remove(mutation.replaceKey)
			}
		}
	}

	private suspend fun advanceMutationVersion() {
		versionMutex.withLock {
			latestMutationVersion += 1
		}
	}

	private suspend fun <T> withScopeExecutionLock(
		scopeKey: ScopeKey,
		block: suspend () -> T
	): T {
		val mutex = executionMutexesMutex.withLock {
			executionMutexes.getOrPut(scopeKey) { Mutex() }
		}

		return mutex.withLock {
			block()
		}
	}
}

private sealed interface MutationExecution<ScopeKey : Any, Command : OutboxMutation, Ack : Any> {
	data class Execute<ScopeKey : Any, Command : OutboxMutation, Ack : Any>(
		val mutation: MutationEnvelope<ScopeKey, Command>,
		val syncSpec: MutationSyncSpec<ScopeKey, Command, Ack>,
		val propagateTerminalErrors: Boolean
	) : MutationExecution<ScopeKey, Command, Ack>

	class Idle<ScopeKey : Any, Command : OutboxMutation, Ack : Any> :
		MutationExecution<ScopeKey, Command, Ack>
}

private sealed interface ExecutionStep<ScopeKey, Command : OutboxMutation> {
	class Done<ScopeKey, Command : OutboxMutation>(
		val result: UpdaterResult
	) : ExecutionStep<ScopeKey, Command>

	class Rebase<ScopeKey, Command : OutboxMutation>(
		val mutation: MutationEnvelope<ScopeKey, Command>
	) : ExecutionStep<ScopeKey, Command>
}
