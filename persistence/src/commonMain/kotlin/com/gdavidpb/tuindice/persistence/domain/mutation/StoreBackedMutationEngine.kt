package com.gdavidpb.tuindice.persistence.domain.mutation

import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcome
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcomeKind
import com.gdavidpb.tuindice.base.domain.model.mutation.OutboxMutation
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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
) : SessionMemory {
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

	// Counts the sessions this engine has seen end. An execution takes the number when it starts and
	// compares it after every wait that a sign-out can cross (the spec's lock-free reads and the
	// download inside resolveFailure): if it moved, the row and the answer belong to the account
	// that left, so the execution neither saves nor sends anything else.
	private val sessionEpoch = MutableStateFlow(0)

	// Held ONLY while the engine writes locally (a row, a confirmation, a verdict), never during a
	// request: the epoch is checked and the write made under it, and clearSessionMemory changes the
	// epoch under it. A write therefore ends before the session does (and the wipe that follows
	// removes it) or finds the new epoch and writes nothing: the session cannot end between the
	// check and the write. Not reentrant, so nothing under it takes it again.
	private val writeGate = Mutex()

	// The terminal results of the mutations this engine settles, for whoever asked for them. Nothing is replayed:
	// a subscriber gets the results from the moment it subscribes on. A result is published only after the write
	// that settles its mutation has ended, never under writeGate.
	private val mutableOutcomes = MutableSharedFlow<MutationOutcome>(extraBufferCapacity = 64)
	val outcomes: SharedFlow<MutationOutcome> = mutableOutcomes.asSharedFlow()

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

	// Forgets what was tracked for the mutations of the account that is leaving: their versions,
	// their failed-sync marks and their runtime state. The version counter keeps counting, so a
	// late result of an old mutation can never match a new one, and the per-scope execution locks
	// stay: they carry no data and one may be held by the very request that ended the session.
	override suspend fun clearSessionMemory() {
		writeGate.withLock { sessionEpoch.value += 1 }
		versionMutex.withLock {
			latestMutationVersionByReplaceKey.clear()
			mutationVersionById.clear()
		}
		runtimeBookkeeperMutex.withLock {
			runtimeBookkeeper.clear()
		}
		runtimeState.value = emptyMap()
	}

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

	// Removes a row without running its spec, because its owner decides so: a reconciliation found it aimed at
	// something the record no longer has, or a parked row was acknowledged. That is a cancellation, so it goes through
	// deletePendingMutation, under writeGate: it publishes CANCELLED for a row whose result was still pending, and
	// nothing for a FailedTerminal one, whose result was published when it failed. Whoever follows the mutation is
	// settled at once, instead of waiting for its limit.
	suspend fun discardMutation(
		scopeKey: ScopeKey,
		mutationId: String
	) {
		deletePendingMutation(scopeKey = scopeKey, mutationId = mutationId)
	}

	/** A newer revision is known: the rows still waiting should expect it (see the store). */
	suspend fun advancePendingRevisions(
		scopeKey: ScopeKey,
		revision: Long
	) {
		outboxStore.advancePendingRevisions(scopeKey = scopeKey, revision = revision)
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
		replaceWaitingMutations(mutation)
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
		replaceWaitingMutations(mutation)
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

		getPendingMutations(scopeKey).forEach { queued ->
			if (targetMutationId != null && queued.mutationId != targetMutationId) {
				return@forEach
			}

			// An earlier row of this pass may have moved the revision the rest expect, so the
			// stored copy is the one to run, not the one read when the pass began. This filter is
			// only a shortcut: the row may change while the execution waits for the scope's lock,
			// so the status is checked again inside it (see alignedMutation).
			val mutation = outboxStore.getPendingMutation(scopeKey, queued.mutationId)
				?.takeIf { stored -> stored.status == PendingMutationStatus.Pending }
				?: return@forEach

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
		// Under writeGate, as a confirmation or a verdict is: the row is either still there and is cancelled here,
		// or it was settled first and its result was published then.
		val cancelled = writeGate.withLock {
			val mutation = outboxStore.getPendingMutation(scopeKey, mutationId)

			outboxStore.deletePendingMutation(scopeKey, mutationId)
			// Only a row that was there moves the version: a delete that finds nothing changes nothing, and moving the
			// version anyway would make a snapshot in flight drop its copy for no reason.
			mutation?.let { deleted ->
				forgetMutationVersion(deleted)
				advanceMutationVersion()
			}

			// A terminal row was published when it ended.
			mutation?.takeIf { deleted -> deleted.status != PendingMutationStatus.FailedTerminal }
		}

		cancelled?.let { deleted -> publishLocalOutcome(deleted.toOutcome(MutationOutcomeKind.CANCELLED)) }
	}

	// Publishes a result the engine did not settle by itself: a mutation its caller resolved without sending it.
	// Nothing checks it and nothing replays it.
	suspend fun publishLocalOutcome(outcome: MutationOutcome) {
		mutableOutcomes.emit(outcome)
	}

	// Replaces the waiting rows that share the replace key of [mutation] with it, and publishes SUPERSEDED for each
	// row it takes out: nothing else would, since a replaced row that was not executing (a Failed one, or one left by
	// an earlier session) is never suppressed. Under writeGate, so the read and the replacement are one write: a
	// confirmation or a verdict that settles one of those rows either ran first (and the row is published here) or
	// finds it gone. A terminal row is left out: its result was published when it ended.
	private suspend fun replaceWaitingMutations(mutation: MutationEnvelope<ScopeKey, Command>) {
		val superseded = writeGate.withLock {
			val replaced = outboxStore.getMutations(mutation.scopeKey).filter { row ->
				row.replaceKey == mutation.replaceKey &&
					row.mutationId != mutation.mutationId &&
					row.status != PendingMutationStatus.FailedTerminal
			}

			outboxStore.replacePendingMutation(mutation)
			replaced
		}

		superseded.forEach { row -> publishLocalOutcome(row.toOutcome(MutationOutcomeKind.SUPERSEDED)) }
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
		val epoch = sessionEpoch.value
		var currentMutation = alignedMutation(execution, epoch)
			?: return UpdaterResult.Success.Untyped(Unit)
		var rebaseAttempts = 0

		while (true) {
			val step = attemptMutation(
				execution = execution,
				mutation = currentMutation,
				rebaseAttempts = rebaseAttempts,
				epoch = epoch
			)

			if (step !is ExecutionStep.Rebase) {
				val done = step as ExecutionStep.Done
				// Published here, once the write that settled the mutation has ended: never under writeGate.
				done.outcome?.let { outcome -> publishLocalOutcome(outcome) }
				return done.result
			}

			rebaseAttempts += 1
			currentMutation = step.mutation

			// The rebase is a verdict on a row the wipe may have taken meanwhile, or one that a newer mutation of the
			// same subject replaced while the failure was resolved: saving it would bring the row back for the account that
			// left, or over the newer mutation. Not saved, the next attempt finds the session changed or the row replaced at
			// its first check and ends without sending.
			writeIfCurrent(epoch) {
				if (shouldApplyMutation(currentMutation)) {
					outboxStore.savePendingMutation(currentMutation)
					currentMutation.expectedRevision?.let { revision ->
						advancePendingRevisions(scopeKey = currentMutation.scopeKey, revision = revision)
					}
				}
			}
		}
	}

	// Runs inside the scope's execution lock, so whatever the mutations ahead of this one
	// confirmed is already stored: the row (not the copy captured when it was queued) is the
	// truth, and the spec may know a newer revision still. The row is also the truth about whether
	// it may be sent at all: an execution that waited for the lock (a drain that saw the row
	// Pending) finds the state the execution ahead of it left, and only a Pending row is sent.
	// One that ended Failed or FailedTerminal stays as it was; returns null then, so the waiting
	// execution touches neither the row nor its version.
	private suspend fun alignedMutation(
		execution: MutationExecution.Execute<ScopeKey, Command, Ack>,
		epoch: Int
	): MutationEnvelope<ScopeKey, Command>? {
		val queued = execution.mutation
		val stored = outboxStore.getPendingMutation(queued.scopeKey, queued.mutationId)

		return when {
			stored == null -> queued

			stored.status != PendingMutationStatus.Pending -> null

			else -> {
				val precondition = execution.syncSpec.currentPrecondition(stored)

				if (sessionEpoch.value != epoch) {
					null
				} else if (precondition == stored.precondition) {
					stored
				} else {
					val aligned = stored.copy(precondition = precondition)

					// Saved only while the row is still this mutation: a row replaced while the precondition was awaited is
					// settled already, and saving it would bring it back. Not saved, the execution ends here without sending.
					val saved = writeIfCurrent(epoch) {
						if (shouldApplyMutation(stored)) {
							outboxStore.savePendingMutation(aligned)
							true
						} else {
							false
						}
					} == true

					if (saved) aligned else null
				}
			}
		}
	}

	private suspend fun attemptMutation(
		execution: MutationExecution.Execute<ScopeKey, Command, Ack>,
		mutation: MutationEnvelope<ScopeKey, Command>,
		rebaseAttempts: Int,
		epoch: Int
	): ExecutionStep<ScopeKey, Command> {
		if (sessionEpoch.value != epoch || !shouldApplyMutation(mutation)) return suppressMutation(mutation)

		return try {
			val ack = execution.syncSpec.send(mutation)

			val confirmed = writeIfCurrent(epoch) {
				if (shouldApplyMutation(mutation)) {
					confirmMutation(execution.syncSpec, mutation, ack)
					true
				} else {
					false
				}
			} == true

			if (confirmed) {
				ExecutionStep.Done(
					result = UpdaterResult.Success.Typed(ack),
					outcome = mutation.toOutcome(MutationOutcomeKind.CONFIRMED)
				)
			} else {
				suppressMutation(mutation)
			}
		} catch (throwable: Throwable) {
			if (throwable is CancellationException) throw throwable

			if (sessionEpoch.value == epoch && shouldApplyMutation(mutation)) {
				resolveExecutionFailure(execution, mutation, throwable, rebaseAttempts, epoch)
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

		syncSpec.revisionOf(ack)?.let { revision ->
			outboxStore.advancePendingRevisions(scopeKey = mutation.scopeKey, revision = revision)
		}

		advanceMutationVersion()
	}

	private suspend fun resolveExecutionFailure(
		execution: MutationExecution.Execute<ScopeKey, Command, Ack>,
		mutation: MutationEnvelope<ScopeKey, Command>,
		throwable: Throwable,
		rebaseAttempts: Int,
		epoch: Int
	): ExecutionStep<ScopeKey, Command> {
		val resolution = runCatching {
			execution.syncSpec.resolveFailure(mutation, throwable)
		}.getOrElse { resolverThrowable ->
			if (resolverThrowable is CancellationException) throw resolverThrowable

			// A resolver that cannot even decide is treated like an exhausted retry: the row
			// survives as Failed so the drain requeue backoff owns the next attempt, and the
			// pass keeps draining instead of aborting on a poison row. Anything else would
			// either lose the mutation or re-send it at full drain cadence forever. A row that a newer mutation of the same
			// subject replaced while the resolver ran is not saved again.
			writeIfCurrent(epoch) {
				if (shouldApplyMutation(mutation)) {
					outboxStore.savePendingMutation(
						mutation.failed(lastError = resolverThrowable.message ?: throwable.message)
					)
				}
			}

			return terminalStep(throwable, execution.propagateTerminalErrors)
		}

		// The resolution may have downloaded for a while: if the session ended meanwhile, the row
		// was wiped with it and saving the verdict would bring it back.
		return writeGate.withLock {
			when {
				sessionEpoch.value != epoch -> ExecutionStep.Done(UpdaterResult.Success.Untyped(Unit))

				else -> applyResolution(
					execution = execution,
					mutation = mutation,
					throwable = throwable,
					rebaseAttempts = rebaseAttempts,
					resolution = resolution
				)
			}
		}
	}

	// Runs [write] only if the session [epoch] belongs to is still the current one, and keeps it from
	// ending until [write] does. Null when the session had ended: nothing was written.
	private suspend fun <T> writeIfCurrent(epoch: Int, write: suspend () -> T): T? {
		return writeGate.withLock {
			if (sessionEpoch.value == epoch) write() else null
		}
	}

	private suspend fun applyResolution(
		execution: MutationExecution.Execute<ScopeKey, Command, Ack>,
		mutation: MutationEnvelope<ScopeKey, Command>,
		throwable: Throwable,
		rebaseAttempts: Int,
		resolution: MutationFailureResolution<ScopeKey, Command>
	): ExecutionStep<ScopeKey, Command> {
		// The verdict writes the row only while it is still this mutation. A row replaced or deleted while the failure was
		// being resolved was settled then (SUPERSEDED or CANCELLED was published): the verdict neither brings it back nor
		// publishes a second result for it. Checked under writeGate, where the row cannot change meanwhile.
		val rowIsCurrent = shouldApplyMutation(mutation)

		return when (resolution) {
			is MutationFailureResolution.Defer -> {
				if (rowIsCurrent) {
					outboxStore.savePendingMutation(
						mutation.retried(lastError = resolution.lastError ?: throwable.message)
					)
				}

				ExecutionStep.Done(UpdaterResult.Success.Untyped(Unit))
			}

			is MutationFailureResolution.Drop -> {
				val outcome = failureOutcome(execution.syncSpec, mutation, throwable)

				if (rowIsCurrent) {
					outboxStore.deletePendingMutation(
						scopeKey = mutation.scopeKey,
						mutationId = mutation.mutationId
					)
				}
				forgetMutationVersion(mutation)
				advanceMutationVersion()

				terminalStep(throwable, resolution.propagate && execution.propagateTerminalErrors, outcome)
			}

			is MutationFailureResolution.Fail -> {
				val outcome = failureOutcome(execution.syncSpec, mutation, throwable)

				if (rowIsCurrent) {
					outboxStore.savePendingMutation(mutation.failedTerminally(throwable.message))
				}

				terminalStep(throwable, resolution.propagate && execution.propagateTerminalErrors, outcome)
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

		// Each exhausted execution is counted for the life of the row: it is requeued after a
		// backoff and would otherwise keep losing the same race forever.
		val exhaustedExecutions = rebasedMutation.rebaseCount + 1
		val parked = rebasedMutation.copy(rebaseCount = exhaustedExecutions)
		val isTerminal = exhaustedExecutions >= execution.syncSpec.maxExhaustedExecutions

		// Only a row parked for good ends here: a Failed one is requeued by a later drain, and its result waits for it.
		val outcome = if (isTerminal) {
			endedOutcome(
				mutation = mutation,
				kind = MutationOutcomeKind.CONFLICT,
				detailCode = execution.syncSpec.classifyError(mutation, throwable).name
			)
		} else {
			null
		}

		// Saved only while the row is still this mutation: a replaced or deleted one was settled already, and saving it
		// would bring it back (see applyResolution).
		if (shouldApplyMutation(mutation)) {
			outboxStore.savePendingMutation(
				if (isTerminal) {
					parked.failedTerminally(throwable.message)
				} else {
					parked.failed(throwable.message)
				}
			)
		}

		return terminalStep(throwable, execution.propagateTerminalErrors, outcome)
	}

	private fun terminalStep(
		throwable: Throwable,
		propagate: Boolean,
		outcome: MutationOutcome? = null
	): ExecutionStep<ScopeKey, Command> {
		val result = if (propagate) {
			UpdaterResult.Error.Exception(throwable)
		} else {
			UpdaterResult.Success.Untyped(Unit)
		}

		return ExecutionStep.Done(result, outcome)
	}

	// The outcome of a mutation that ends here, or null when its row is no longer the one this execution decides: a row
	// replaced or deleted while its failure was being resolved was already published (SUPERSEDED or CANCELLED). Checked
	// before the row is written, since the write takes it out.
	private suspend fun endedOutcome(
		mutation: MutationEnvelope<ScopeKey, Command>,
		kind: MutationOutcomeKind,
		detailCode: String? = null
	): MutationOutcome? {
		if (!shouldApplyMutation(mutation)) return null

		return mutation.toOutcome(kind, detailCode)
	}

	// A conflict or a precondition failure is a CONFLICT, any other failure a REJECTED. The detail is the kind the spec
	// classified the failure as, not the text of the failure.
	private suspend fun failureOutcome(
		syncSpec: MutationSyncSpec<ScopeKey, Command, Ack>,
		mutation: MutationEnvelope<ScopeKey, Command>,
		throwable: Throwable
	): MutationOutcome? {
		val failure = syncSpec.classifyError(mutation, throwable)
		val kind = when (failure) {
			MutationFailureKind.Conflict,
			MutationFailureKind.PreconditionFailed -> MutationOutcomeKind.CONFLICT

			MutationFailureKind.NotFound,
			MutationFailureKind.Terminal -> MutationOutcomeKind.REJECTED
		}

		return endedOutcome(mutation, kind, failure.name)
	}

	private fun MutationEnvelope<ScopeKey, Command>.toOutcome(
		kind: MutationOutcomeKind,
		detailCode: String? = null
	) = MutationOutcome(
		mutationId = mutationId,
		kind = kind,
		detailCode = detailCode
	)

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
		val result: UpdaterResult,
		// The terminal result this step settled, when it settled one (see MutationOutcome).
		val outcome: MutationOutcome? = null
	) : ExecutionStep<ScopeKey, Command>

	class Rebase<ScopeKey, Command : OutboxMutation>(
		val mutation: MutationEnvelope<ScopeKey, Command>
	) : ExecutionStep<ScopeKey, Command>
}
