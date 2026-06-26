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

@OptIn(ExperimentalStoreApi::class)
class StoreBackedMutationEngine<ScopeKey : Any, Command : OutboxMutation, ConfirmedState, VisibleState, Ack : Any>(
	private val storeId: String,
	private val outboxStore: MutationEnvelopeStore<ScopeKey, Command>,
	private val coroutineScope: CoroutineScope
) {
	private val versionMutex = Mutex()
	private val runtimeBookkeeperMutex = Mutex()
	private val executionMutexesMutex = Mutex()
	private val latestMutationVersionByReplaceKey = mutableMapOf<String, Long>()
	private val mutationVersionById = mutableMapOf<String, Long>()
	private val runtimeBookkeeper = mutableMapOf<ScopeKey, Long>()
	private val executionMutexes = mutableMapOf<ScopeKey, Mutex>()
	private val runtimeState =
		MutableStateFlow<Map<ScopeKey, MutationExecution<ScopeKey, Command, ConfirmedState, VisibleState, Ack>>>(
			emptyMap()
		)

	private var latestMutationVersion = 0L

	private val runtimeStore: MutableStore<ScopeKey, MutationExecution<ScopeKey, Command, ConfirmedState, VisibleState, Ack>> =
		MutableStoreBuilder.from<
				ScopeKey,
				MutationExecution<ScopeKey, Command, ConfirmedState, VisibleState, Ack>,
				MutationExecution<ScopeKey, Command, ConfirmedState, VisibleState, Ack>,
				MutationExecution<ScopeKey, Command, ConfirmedState, VisibleState, Ack>
				>(
			fetcher = Fetcher.of(name = "$storeId-runtime") { _: ScopeKey ->
				MutationExecution.Idle()
			},
			sourceOfTruth = object : SourceOfTruth<
					ScopeKey,
					MutationExecution<ScopeKey, Command, ConfirmedState, VisibleState, Ack>,
					MutationExecution<ScopeKey, Command, ConfirmedState, VisibleState, Ack>
					> {
				override fun reader(
					key: ScopeKey
				) = runtimeState.map { executions ->
					executions[key] ?: MutationExecution.Idle()
				}

				override suspend fun write(
					key: ScopeKey,
					value: MutationExecution<ScopeKey, Command, ConfirmedState, VisibleState, Ack>
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
					MutationExecution<ScopeKey, Command, ConfirmedState, VisibleState, Ack>,
					MutationExecution<ScopeKey, Command, ConfirmedState, VisibleState, Ack>,
					MutationExecution<ScopeKey, Command, ConfirmedState, VisibleState, Ack>
					> {
				override fun fromNetworkToLocal(
					network: MutationExecution<ScopeKey, Command, ConfirmedState, VisibleState, Ack>
				): MutationExecution<ScopeKey, Command, ConfirmedState, VisibleState, Ack> = network

				override fun fromOutputToLocal(
					output: MutationExecution<ScopeKey, Command, ConfirmedState, VisibleState, Ack>
				): MutationExecution<ScopeKey, Command, ConfirmedState, VisibleState, Ack> = output
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
		syncSpec: MutationSyncSpec<ScopeKey, Command, ConfirmedState, VisibleState, Ack>,
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
		syncSpec: MutationSyncSpec<ScopeKey, Command, ConfirmedState, VisibleState, Ack>,
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
		syncSpec: MutationSyncSpec<ScopeKey, Command, ConfirmedState, VisibleState, Ack>,
		propagateTerminalErrors: Boolean = false,
		targetMutationId: String? = null
	) {
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
		outboxStore.deletePendingMutation(scopeKey, mutationId)
		forgetMutationVersion(mutationId)
	}

	private suspend fun executeMutation(
		execution: MutationExecution.Execute<ScopeKey, Command, ConfirmedState, VisibleState, Ack>
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
		execution: MutationExecution.Execute<ScopeKey, Command, ConfirmedState, VisibleState, Ack>
	): UpdaterResult {
		val syncSpec = execution.syncSpec
		var currentMutation = execution.mutation
		var rebaseAttempts = 0

		while (true) {
			if (!shouldApplyMutation(currentMutation)) {
				forgetMutationVersion(currentMutation.mutationId)
				return UpdaterResult.Success.Untyped(Unit)
			}

			try {
				val ack = syncSpec.send(currentMutation)

				if (!shouldApplyMutation(currentMutation)) {
					forgetMutationVersion(currentMutation.mutationId)
					return UpdaterResult.Success.Untyped(Unit)
				}

				if (syncSpec.deletePendingBeforeConfirm(currentMutation)) {
					outboxStore.deletePendingMutation(
						scopeKey = currentMutation.scopeKey,
						mutationId = currentMutation.mutationId
					)
					forgetMutationVersion(currentMutation.mutationId)
					syncSpec.confirm(currentMutation, ack)
				} else {
					syncSpec.confirm(currentMutation, ack)
					outboxStore.deletePendingMutation(
						scopeKey = currentMutation.scopeKey,
						mutationId = currentMutation.mutationId
					)
					forgetMutationVersion(currentMutation.mutationId)
				}

				return UpdaterResult.Success.Typed(ack)
			} catch (throwable: Throwable) {
				if (throwable is CancellationException) throw throwable

				if (!shouldApplyMutation(currentMutation)) {
					forgetMutationVersion(currentMutation.mutationId)
					return UpdaterResult.Success.Untyped(Unit)
				}

				when (val resolution = syncSpec.resolveFailure(currentMutation, throwable)) {
					is MutationFailureResolution.Defer -> {
						outboxStore.savePendingMutation(
							currentMutation.copy(
								status = PendingMutationStatus.Pending,
								updatedAt = currentTimeMillis(),
								lastError = resolution.lastError ?: throwable.message
							)
						)

						return UpdaterResult.Success.Untyped(Unit)
					}

					is MutationFailureResolution.Drop -> {
						outboxStore.deletePendingMutation(
							scopeKey = currentMutation.scopeKey,
							mutationId = currentMutation.mutationId
						)
						forgetMutationVersion(currentMutation.mutationId)

						return if (resolution.propagate && execution.propagateTerminalErrors) {
							UpdaterResult.Error.Exception(throwable)
						} else {
							UpdaterResult.Success.Untyped(Unit)
						}
					}

					is MutationFailureResolution.Fail -> {
						outboxStore.savePendingMutation(
							currentMutation.copy(
								status = PendingMutationStatus.Failed,
								updatedAt = currentTimeMillis(),
								lastError = throwable.message
							)
						)

						return if (resolution.propagate && execution.propagateTerminalErrors) {
							UpdaterResult.Error.Exception(throwable)
						} else {
							UpdaterResult.Success.Untyped(Unit)
						}
					}

					is MutationFailureResolution.Retry -> {
						val rebasedMutation = resolution.mutation.copy(
							status = PendingMutationStatus.Pending,
							updatedAt = currentTimeMillis(),
							lastError = null
						)

						if (
							rebasedMutation.precondition == currentMutation.precondition ||
							rebaseAttempts >= syncSpec.maxRebaseAttempts
						) {
							outboxStore.savePendingMutation(
								rebasedMutation.copy(
									status = PendingMutationStatus.Failed,
									updatedAt = currentTimeMillis(),
									lastError = throwable.message
								)
							)

							return if (execution.propagateTerminalErrors) {
								UpdaterResult.Error.Exception(throwable)
							} else {
								UpdaterResult.Success.Untyped(Unit)
							}
						}

						rebaseAttempts += 1
						currentMutation = rebasedMutation
						outboxStore.savePendingMutation(currentMutation)
					}
				}
			}
		}
	}

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
		mutationId: String
	) {
		versionMutex.withLock {
			mutationVersionById.remove(mutationId)
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

private sealed interface MutationExecution<ScopeKey : Any, Command : OutboxMutation, ConfirmedState, VisibleState, Ack : Any> {
	data class Execute<ScopeKey : Any, Command : OutboxMutation, ConfirmedState, VisibleState, Ack : Any>(
		val mutation: MutationEnvelope<ScopeKey, Command>,
		val syncSpec: MutationSyncSpec<ScopeKey, Command, ConfirmedState, VisibleState, Ack>,
		val propagateTerminalErrors: Boolean
	) : MutationExecution<ScopeKey, Command, ConfirmedState, VisibleState, Ack>

	class Idle<ScopeKey : Any, Command : OutboxMutation, ConfirmedState, VisibleState, Ack : Any> :
		MutationExecution<ScopeKey, Command, ConfirmedState, VisibleState, Ack>
}
