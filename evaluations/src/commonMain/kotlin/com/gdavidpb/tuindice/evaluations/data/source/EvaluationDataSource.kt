@file:OptIn(ExperimentalTime::class, ExperimentalAtomicApi::class)

package com.gdavidpb.tuindice.evaluations.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcome
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcomeKind
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.base.domain.repository.IdentifierRepository
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.base.utils.isWithinWindow
import com.gdavidpb.tuindice.evaluations.data.mapper.toEditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.data.mapper.toEvaluation
import com.gdavidpb.tuindice.evaluations.data.mapper.toEvaluationDisplayContext
import com.gdavidpb.tuindice.evaluations.data.mapper.toEvaluationTermDescriptor
import com.gdavidpb.tuindice.evaluations.data.mapper.toLocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluationsSnapshot
import com.gdavidpb.tuindice.evaluations.data.model.RemoteEvaluationsSnapshot
import com.gdavidpb.tuindice.evaluations.data.mutation.EVALUATIONS_MUTATION_SCOPE
import com.gdavidpb.tuindice.evaluations.data.mutation.EvaluationMutation
import com.gdavidpb.tuindice.evaluations.data.mutation.EvaluationMutationAck
import com.gdavidpb.tuindice.evaluations.data.mutation.EvaluationMutationSyncSpec
import com.gdavidpb.tuindice.evaluations.data.repository.DatabaseDataRepository
import com.gdavidpb.tuindice.evaluations.data.repository.EvaluationsApiDataRepository
import com.gdavidpb.tuindice.evaluations.data.repository.SettingsDataRepository
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDisplayContext
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationsRefreshResult
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationsRevalidationRepository
import com.gdavidpb.tuindice.evaluations.utils.CooldownTimes
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationPrecondition
import com.gdavidpb.tuindice.persistence.domain.mutation.StoreBackedMutationEngine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.Volatile
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class EvaluationDataSource(
	private val databaseDataSource: DatabaseDataRepository,
	private val evaluationsApiDataSource: EvaluationsApiDataRepository,
	private val settingsDataSource: SettingsDataRepository,
	private val mutationEngine: StoreBackedMutationEngine<String, EvaluationMutation, EvaluationMutationAck>,
	private val identifierRepository: IdentifierRepository,
	private val clock: Clock
) : EvaluationRepository, EvaluationsRevalidationRepository, SessionMemory {
	// Serializes the decision to download, the download and its save, so the tab and the
	// revalidation after a sync never download twice at once. It does NOT cover the draining of the
	// pending changes nor the result: the engine serializes those per scope, and a student's write
	// never waits for a download. refreshRemoteSnapshot (a 409/412/404 met while draining) is the
	// other download and deliberately does not take it: a drain must never need this lock, and the
	// mutex is not reentrant.
	private val updateMutex = Mutex()

	// The download currently being decided or made, if any: registered by the first caller that does
	// not force one BEFORE it waits for the lock or reads anything, so a caller that does not force a
	// download and arrives while somebody decides or downloads joins that one instead of deciding
	// again or queueing for a second request. Compare-and-set, because callers register without the
	// lock; whoever downloads sets it again (a forced caller registers there).
	private val inFlight = AtomicReference<Download?>(null)

	// Held ONLY while the answer of a download is written locally (the snapshot, the cooldown, the
	// failure mark), never during a request, and taken by clearSessionMemory to change the epoch. A
	// write therefore ends before the session does (and the wipe that follows removes it) or finds
	// the new epoch and writes nothing: the epoch cannot change between its check and its save.
	private val writeGate = Mutex()

	// Bumped by clearSessionMemory. A download remembers the one it started in: if it differs when
	// the answer arrives, the account that asked is gone and the answer must leave no trace.
	@Volatile
	private var sessionEpoch: Int = 0

	// When the last download failed; an automatic revalidation waits COOLDOWN_RETRY_EVALUATIONS after
	// it. In memory on purpose: a fresh process is a fair moment to try again. Only the revalidation
	// is held by it: the tab, which the student opened on purpose, always tries. Written only by a
	// download of the current epoch.
	@Volatile
	private var lastDownloadFailureAt: Long? = null

	private val mutationSyncSpec = EvaluationMutationSyncSpec(
		databaseDataSource = databaseDataSource,
		evaluationsApiDataSource = evaluationsApiDataSource,
		refreshRemoteSnapshot = ::refreshRemoteSnapshot
	)

	override suspend fun observeEvaluationsFlow(): Flow<List<Evaluation>> {
		return databaseDataSource.observeEvaluationsFlow()
			.map { evaluations -> evaluations.map { evaluation -> evaluation.toEvaluation(clock) } }
	}

	override suspend fun observeHasSyncedEvaluationsFlow(): Flow<Boolean> {
		return databaseDataSource.observeHasSyncedEvaluationsFlow()
	}

	override suspend fun observeEvaluationsSnapshotFlow(): Flow<ObservedSyncedSnapshot<List<Evaluation>>> {
		return databaseDataSource.observeEvaluationsSnapshotFlow()
			.map { snapshot -> snapshot.toObservedSyncedSnapshot() }
	}

	override suspend fun getEvaluationsSnapshot(): ObservedSyncedSnapshot<List<Evaluation>> {
		return databaseDataSource.getEvaluationsSnapshot().toObservedSyncedSnapshot()
	}

	override suspend fun updateEvaluations(): EvaluationsRefreshResult {
		return updateEvaluations(forceRemote = false)
	}

	// The hold belongs to the account whose download failed; the next one starts without it. Not
	// under updateMutex: a request made while holding it can end the session, and the wipe would
	// then wait for its own caller. Under writeGate, which only a local write holds: the epoch
	// changes between two writes, never in the middle of one.
	//
	// The request of the download in progress is cancelled, so the lock it holds is free at once
	// instead of after the timeout of a request nobody is waiting for; whoever joined it starts over.
	// The wipe never runs inside a request (the invalidation belongs to the app), so the caller is never
	// the request being cancelled.
	override suspend fun clearSessionMemory() {
		writeGate.withLock {
			sessionEpoch += 1
			lastDownloadFailureAt = null
		}

		inFlight.load()?.request?.cancel()
	}

	override suspend fun revalidateEvaluationsIfNeverDownloaded() {
		if (databaseDataSource.observeHasSyncedEvaluationsFlow().first()) return

		revalidateEvaluations()
	}

	override suspend fun revalidateEvaluations() {
		val outcome = fetchIfNeeded(forceRemote = false, respectHold = true)

		if (outcome == FetchOutcome.OnHold) return

		drainPendingMutations()
	}

	override suspend fun updateEvaluations(forceRemote: Boolean): EvaluationsRefreshResult {
		val outcome = fetchIfNeeded(forceRemote = forceRemote, respectHold = false)

		drainPendingMutations()

		val visibleSnapshot = databaseDataSource.getEvaluationsSnapshot()
		val availableAttemptIds = databaseDataSource.getAvailableAttempts()
			.mapTo(HashSet()) { attempt -> attempt.id }
		val fetchedSnapshot = (outcome as? FetchOutcome.Fetched)?.snapshot
		// An evaluation counts only under a subject of the current term: one whose subject is gone
		// is never listed, so it must not keep the screen waiting for a list that will not come.
		return EvaluationsRefreshResult(
			hasEvaluations = fetchedSnapshot?.evaluations
				?.any { evaluation -> evaluation.attemptId in availableAttemptIds } == true ||
				visibleSnapshot.evaluations
					.any { evaluation -> evaluation.attemptId in availableAttemptIds },
			hasAvailableAttempts = availableAttemptIds.isNotEmpty(),
			hasSchedule = databaseDataSource.observeDisplayContextFlow().first().hasSchedule
		)
	}

	// A caller that does not force a download and finds one in progress, even one that is still
	// deciding, waits for THAT download: it uses the snapshot if it arrives, and gets the very same
	// failure if it does not, without a second request. A forced caller queues for the lock and
	// downloads after it. If the download it joined was cancelled (not failed) or ended up not
	// downloading, nobody got an answer, so it starts over.
	private suspend fun fetchIfNeeded(forceRemote: Boolean, respectHold: Boolean): FetchOutcome {
		while (true) {
			val claim = claimDownload(forceRemote)
			val joined = claim.joined

			if (joined != null) {
				val answer = joined.answer.await() ?: continue

				return FetchOutcome.Fetched(answer.getOrThrow())
			}

			val own = claim.own

			try {
				return updateMutex.withLock { fetchLocked(own, forceRemote, respectHold) }
			} finally {
				own?.let(::retire)
			}
		}
	}

	// Joins the download being decided or made, or registers this caller as the one that decides:
	// with no suspension in between, so two callers never both decide.
	private fun claimDownload(forceRemote: Boolean): Claim {
		if (forceRemote) return Claim(joined = null, own = null)

		var claim: Claim? = null

		while (claim == null) {
			val current = inFlight.load()

			claim = if (current != null && current.epoch == sessionEpoch) {
				Claim(joined = current, own = null)
			} else {
				val own = Download(epoch = sessionEpoch)

				if (inFlight.compareAndSet(current, own)) Claim(joined = null, own = own) else null
			}
		}

		return claim
	}

	// Cooldown, hold and "already downloaded" are read inside the lock, so a caller queued behind a
	// download skips its own.
	private suspend fun fetchLocked(own: Download?, forceRemote: Boolean, respectHold: Boolean): FetchOutcome {
		return when {
			respectHold && isRetryOnHold() -> FetchOutcome.OnHold
			!forceRemote && isAlreadyDownloaded() -> FetchOutcome.NotNeeded
			else -> FetchOutcome.Fetched(downloadSharing(own ?: Download(epoch = sessionEpoch)))
		}
	}

	private suspend fun isAlreadyDownloaded(): Boolean {
		val isOnCooldown = settingsDataSource.isGetEvaluationsOnCooldown()
		val hasSyncedEvaluations = databaseDataSource.observeHasSyncedEvaluationsFlow().first()

		return isOnCooldown && hasSyncedEvaluations
	}

	private suspend fun downloadSharing(download: Download): RemoteEvaluationsSnapshot {
		var answer: Result<RemoteEvaluationsSnapshot>? = null

		// A caller of a session that already ended has nobody to answer to, and it is checked BEFORE the
		// download is registered: a registration of the old session would replace the one the new session
		// made while this caller waited for the lock, and removing it afterwards would leave whoever
		// arrives next with nothing to share.
		if (download.epoch != sessionEpoch) {
			retire(download)
			throw CancellationException("The session ended before the download began")
		}

		inFlight.store(download)
		try {
			answer = runCatching { downloadAndSave(download) }
				.onFailure { throwable -> if (throwable is CancellationException) throw throwable }
		} finally {
			// A null answer means the owner was cancelled: joiners start over instead of inheriting
			// a cancellation that is not theirs.
			inFlight.compareAndSet(download, null)
			download.answer.complete(answer)
		}

		return checkNotNull(answer).getOrThrow()
	}

	// The download that did not end up downloading (nothing was needed, the retry is on hold, the
	// owner was cancelled while it waited): nobody got an answer, and those who joined start over.
	private fun retire(download: Download) {
		inFlight.compareAndSet(download, null)
		download.answer.complete(null)
	}

	private fun isRetryOnHold(): Boolean {
		val failedAt = lastDownloadFailureAt ?: return false

		return isWithinWindow(
			sinceMillis = failedAt,
			nowMillis = clock.now().toEpochMilliseconds(),
			windowMillis = CooldownTimes.COOLDOWN_RETRY_EVALUATIONS
		)
	}

	// Only a download of the current epoch leaves a trace: a failure stamp, the snapshot, the
	// cooldown. One that outlived its session (the tab's runs in the screen's scope, not the
	// session's) would otherwise hand the previous account's evaluations to the next one. The epoch
	// is checked and the answer saved under writeGate, so the session cannot end in between.
	private suspend fun downloadAndSave(download: Download): RemoteEvaluationsSnapshot {
		val epoch = download.epoch
		val snapshotVersion = mutationEngine.currentMutationVersion()
		val remoteSnapshot = runCatching { requestEvaluations(download) }
			.onFailure { throwable ->
				if (throwable !is CancellationException) {
					writeGate.withLock {
						if (epoch == sessionEpoch) lastDownloadFailureAt = clock.now().toEpochMilliseconds()
					}
				}
			}
			.getOrThrow()

		writeGate.withLock {
			if (epoch != sessionEpoch) return remoteSnapshot

			lastDownloadFailureAt = null
			if (snapshotVersion == mutationEngine.currentMutationVersion()) {
				databaseDataSource.saveConfirmedSnapshot(remoteSnapshot.toLocalSnapshot())
				settingsDataSource.setGetEvaluationsOnCooldown()
			}
		}

		return remoteSnapshot
	}

	// The request runs as a job of its own, so that the end of the session can cancel it (see
	// clearSessionMemory) without cancelling whoever asked. Its outcome travels as a value: a
	// failure is rethrown here exactly as it came.
	private suspend fun requestEvaluations(download: Download): RemoteEvaluationsSnapshot {
		val outcome = coroutineScope {
			val request = async { runCatching { evaluationsApiDataSource.getEvaluations() } }

			download.request = request
			// The session may have ended before the request could be found.
			if (download.epoch != sessionEpoch) request.cancel()

			request.await()
		}

		return outcome.getOrThrow()
	}

	override suspend fun drainPendingMutations() {
		mutationEngine.drain(
			scopeKey = EVALUATIONS_MUTATION_SCOPE,
			syncSpec = mutationSyncSpec,
			propagateTerminalErrors = false
		)
	}

	override suspend fun getEvaluation(eid: String): Evaluation? {
		return databaseDataSource.getEvaluation(eid)?.toEvaluation(clock)
	}

	override suspend fun addEvaluation(add: EvaluationAdd) {
		val command = EvaluationMutation.Add(
			referenceId = add.reference,
			attemptId = add.attemptId,
			subjectCode = add.subjectCode,
			termId = add.termId,
			scheduleMode = add.scheduleMode,
			grade = add.grade,
			maxGrade = add.maxGrade,
			date = add.date,
			type = add.type.ordinal
		)
		val mutation = buildPendingAddMutation(
			command = equivalentPendingAdd(command) ?: command,
			requestedMutationId = add.mutationId
		)
		val mutationVersion = mutationEngine.beginMutation(replaceKey = mutation.replaceKey)
		mutationEngine.rememberMutationVersion(mutation.mutationId, mutationVersion)
		mutationEngine.submitInBackground(
			mutation = mutation,
			syncSpec = mutationSyncSpec,
			propagateTerminalErrors = false
		)
	}

	override suspend fun updateEvaluation(update: EvaluationUpdate) {
		val pendingAdd = pendingAddForReference(update.id)
		if (pendingAdd != null) {
			val rewrittenAdd = (pendingAdd.command as EvaluationMutation.Add).apply(update)
			val mutation = buildPendingAddMutation(
				command = rewrittenAdd,
				requestedMutationId = update.mutationId
			)
			val mutationVersion = mutationEngine.beginMutation(replaceKey = mutation.replaceKey)
			mutationEngine.rememberMutationVersion(mutation.mutationId, mutationVersion)
			mutationEngine.submitInBackground(
				mutation = mutation,
				syncSpec = mutationSyncSpec,
				propagateTerminalErrors = false
			)
			return
		}

		val evaluation = databaseDataSource.getEvaluation(update.id)
			?: return
		val mutation = buildPendingUpdateMutation(
			evaluationId = evaluation.id,
			update = update,
			expectedRevision = evaluation.revision,
			requestedMutationId = update.mutationId
		)
		val mutationVersion = mutationEngine.beginMutation(replaceKey = mutation.replaceKey)
		mutationEngine.rememberMutationVersion(mutation.mutationId, mutationVersion)
		mutationEngine.submitInBackground(
			mutation = mutation,
			syncSpec = mutationSyncSpec,
			propagateTerminalErrors = false
		)
	}

	override suspend fun removeEvaluation(remove: EvaluationRemove) {
		val pendingAdd = pendingAddForReference(remove.id)
		if (pendingAdd != null) {
			mutationEngine.deletePendingMutation(
				scopeKey = EVALUATIONS_MUTATION_SCOPE,
				mutationId = pendingAdd.mutationId
			)
			// The add never reached the server: removing it is settled here, and nothing is sent for the removal.
			mutationEngine.publishLocalOutcome(
				MutationOutcome(
					mutationId = remove.mutationId ?: identifierRepository.generateRandomIdentifier(),
					kind = MutationOutcomeKind.CONFIRMED
				)
			)
			return
		}

		val evaluation = databaseDataSource.getEvaluation(remove.id)
			?: return
		val mutation = buildPendingRemoveMutation(
			evaluationId = evaluation.id,
			expectedRevision = evaluation.revision,
			requestedMutationId = remove.mutationId
		)
		val mutationVersion = mutationEngine.beginMutation(replaceKey = mutation.replaceKey)
		mutationEngine.rememberMutationVersion(mutation.mutationId, mutationVersion)
		mutationEngine.submitInBackground(
			mutation = mutation,
			syncSpec = mutationSyncSpec,
			propagateTerminalErrors = false
		)
	}

	override suspend fun getAvailableAttempts(): List<EditableAttemptDescriptor> {
		return databaseDataSource.getAvailableAttempts()
			.map { attempt -> attempt.toEditableAttemptDescriptor() }
			.sortedBy(EditableAttemptDescriptor::code)
	}

	override suspend fun getCurrentTerm() =
		databaseDataSource.getCurrentTerm()?.toEvaluationTermDescriptor()

	override fun observeDisplayContextFlow(): Flow<EvaluationDisplayContext> {
		return databaseDataSource.observeDisplayContextFlow()
			.map { context -> context.toEvaluationDisplayContext() }
			.distinctUntilChanged()
	}

	// The engine is this module's alone (its own store), so every outcome it publishes is one of these mutations.
	override fun observeMutationOutcomesFlow(): Flow<MutationOutcome> = mutationEngine.outcomes

	private fun LocalEvaluationsSnapshot.toObservedSyncedSnapshot(): ObservedSyncedSnapshot<List<Evaluation>> {
		return ObservedSyncedSnapshot(
			value = evaluations.map { evaluation -> evaluation.toEvaluation(clock) },
			hasSynced = hasSynced
		)
	}

	// The download a 409, 412 or 404 asks for while a drain is running. It must not take updateMutex:
	// a drain runs outside that lock and must never wait for it (the mutex is not reentrant, and a
	// caller that holds it may itself be waiting on a drain's scope). The mutation version check
	// below is what keeps it from overwriting newer writes.
	private suspend fun refreshRemoteSnapshot(): RemoteEvaluationsSnapshot {
		val epoch = sessionEpoch
		val snapshotVersion = mutationEngine.currentMutationVersion()
		val remoteSnapshot = evaluationsApiDataSource.getEvaluations()
		// Like downloadAndSave: an answer that outlived its session is not stored for the next account,
		// and the session cannot end between the check and the save.
		writeGate.withLock {
			if (epoch == sessionEpoch && snapshotVersion == mutationEngine.currentMutationVersion()) {
				databaseDataSource.saveConfirmedSnapshot(remoteSnapshot.toLocalSnapshot())
			}
		}
		return remoteSnapshot
	}

	private suspend fun equivalentPendingAdd(
		command: EvaluationMutation.Add
	): EvaluationMutation.Add? {
		return mutationEngine.getMutations(EVALUATIONS_MUTATION_SCOPE)
			.asSequence()
			.map(MutationEnvelope<String, EvaluationMutation>::command)
			.filterIsInstance<EvaluationMutation.Add>()
			.firstOrNull { pending -> command.copy(referenceId = pending.referenceId) == pending }
	}

	private suspend fun pendingAddForReference(
		referenceId: String
	): MutationEnvelope<String, EvaluationMutation>? {
		return mutationEngine.getMutations(EVALUATIONS_MUTATION_SCOPE)
			.firstOrNull { mutation ->
				val command = mutation.command
				command is EvaluationMutation.Add && command.referenceId == referenceId
			}
	}

	private fun buildPendingAddMutation(
		command: EvaluationMutation.Add,
		requestedMutationId: String?
	): MutationEnvelope<String, EvaluationMutation> {
		val now = currentTimeMillis()
		return MutationEnvelope(
			mutationId = requestedMutationId ?: identifierRepository.generateRandomIdentifier(),
			scopeKey = EVALUATIONS_MUTATION_SCOPE,
			command = command,
			precondition = MutationPrecondition.None,
			status = PendingMutationStatus.Pending,
			createdAt = now,
			updatedAt = now,
			lastError = null
		)
	}

	private fun buildPendingUpdateMutation(
		evaluationId: String,
		update: EvaluationUpdate,
		expectedRevision: Long,
		requestedMutationId: String?
	): MutationEnvelope<String, EvaluationMutation> {
		val now = currentTimeMillis()
		return MutationEnvelope(
			mutationId = requestedMutationId ?: identifierRepository.generateRandomIdentifier(),
			scopeKey = EVALUATIONS_MUTATION_SCOPE,
			command = EvaluationMutation.Update(
				evaluationId = evaluationId,
				scheduleMode = update.scheduleMode,
				grade = update.grade,
				maxGrade = update.maxGrade,
				date = update.date,
				type = update.type?.ordinal
			),
			precondition = MutationPrecondition.Revision(expectedRevision),
			status = PendingMutationStatus.Pending,
			createdAt = now,
			updatedAt = now,
			lastError = null
		)
	}

	private fun buildPendingRemoveMutation(
		evaluationId: String,
		expectedRevision: Long,
		requestedMutationId: String?
	): MutationEnvelope<String, EvaluationMutation> {
		val now = currentTimeMillis()
		return MutationEnvelope(
			mutationId = requestedMutationId ?: identifierRepository.generateRandomIdentifier(),
			scopeKey = EVALUATIONS_MUTATION_SCOPE,
			command = EvaluationMutation.Remove(evaluationId = evaluationId),
			precondition = MutationPrecondition.Revision(expectedRevision),
			status = PendingMutationStatus.Pending,
			createdAt = now,
			updatedAt = now,
			lastError = null
		)
	}

	private fun RemoteEvaluationsSnapshot.toLocalSnapshot() = LocalEvaluationsSnapshot(
		hasSynced = true,
		evaluations = evaluations.map { evaluation -> evaluation.toLocalEvaluation() }
	)

	private fun EvaluationMutation.Add.apply(update: EvaluationUpdate): EvaluationMutation.Add {
		val resolvedScheduleMode = update.scheduleMode ?: if (update.date != null) {
			EvaluationScheduleMode.DATED
		} else {
			scheduleMode
		}
		val resolvedDate = when (resolvedScheduleMode) {
			EvaluationScheduleMode.CONTINUOUS -> null
			EvaluationScheduleMode.DATED -> update.date ?: date
		}
		val resolvedGrade = update.grade

		return copy(
			scheduleMode = resolvedScheduleMode,
			grade = resolvedGrade,
			maxGrade = update.maxGrade ?: maxGrade,
			date = resolvedDate,
			type = update.type?.ordinal ?: type
		)
	}

}

private class Download(val epoch: Int) {
	// null: the owner was cancelled, or did not download after all.
	val answer = CompletableDeferred<Result<RemoteEvaluationsSnapshot>?>()

	// The request, once it is made: what the end of the session cancels.
	@Volatile
	var request: Job? = null
}

private class Claim(val joined: Download?, val own: Download?)

private sealed interface FetchOutcome {
	data object NotNeeded : FetchOutcome
	data object OnHold : FetchOutcome
	data class Fetched(val snapshot: RemoteEvaluationsSnapshot) : FetchOutcome
}
