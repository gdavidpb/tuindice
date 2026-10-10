package com.gdavidpb.tuindice.pensum.data.source

import com.gdavidpb.tuindice.base.domain.repository.RecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.base.utils.extension.isNotFound
import com.gdavidpb.tuindice.pensum.data.mapper.toAvailableModalities
import com.gdavidpb.tuindice.pensum.data.mapper.toAvailablePensums
import com.gdavidpb.tuindice.pensum.data.mapper.toGraph
import com.gdavidpb.tuindice.pensum.data.mapper.toGraphs
import com.gdavidpb.tuindice.pensum.data.mapper.toSelection
import com.gdavidpb.tuindice.pensum.data.model.GetPensumResponse
import com.gdavidpb.tuindice.pensum.data.model.SelectedPensumCacheState
import com.gdavidpb.tuindice.pensum.data.repository.PensumLocalDataRepository
import com.gdavidpb.tuindice.pensum.data.repository.PensumRemoteDataRepository
import com.gdavidpb.tuindice.pensum.domain.engine.PensumStatusEngine
import com.gdavidpb.tuindice.pensum.domain.model.ObservedPensum
import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.pensum.domain.model.PensumSelectionParams
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRepository
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRevalidationRepository
import com.gdavidpb.tuindice.pensum.utils.CooldownTimes
import com.gdavidpb.tuindice.pensum.utils.isWithinWindow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PensumDataSource(
	private val localDataRepository: PensumLocalDataRepository,
	private val remoteDataRepository: PensumRemoteDataRepository,
	private val pensumStatusEngine: PensumStatusEngine,
	private val recordDataPrerequisiteRepository: RecordDataPrerequisiteRepository
) : PensumRepository, PensumRevalidationRepository, SessionMemory {
	// Every remote read-then-persist runs under this lock: a revalidation that lands after the
	// student switched pensum would otherwise write back the selection they just left.
	private val remoteWriteMutex = Mutex()

	// When the last automatic revalidation failed; the next one waits COOLDOWN_RETRY_PENSUM. In
	// memory on purpose: a fresh process is a fair moment to try again. Guarded by remoteWriteMutex.
	private var lastRevalidationFailureAt: Long? = null

	// The hold belongs to the account whose revalidation failed; the next one starts without it.
	// Not under remoteWriteMutex: a request made while holding it can end the session, and the
	// wipe would then wait for its own caller.
	override suspend fun clearSessionMemory() {
		lastRevalidationFailureAt = null
	}

	override fun observePensumFlow(): Flow<PensumObservation> {
		return combine(
			localDataRepository.observePensumResponseFlow(),
			localDataRepository.observeAcademicSnapshotFlow(),
			recordDataPrerequisiteRepository.observeRecordDataPrerequisiteFlow()
		) { response, academicSnapshot, prerequisite ->
			when {
				prerequisite.hasFailed -> return@combine PensumObservation.RecordDataUnavailable
				!prerequisite.isReady -> return@combine PensumObservation.WaitingForRecordData
				response == null -> return@combine PensumObservation.Missing
			}

			checkNotNull(response)
			val graph = response.toGraph()
			val graphs = response.toGraphs()
			val progress = pensumStatusEngine.resolve(
				pensum = graph,
				academicSnapshot = academicSnapshot
			)

			PensumObservation.Content(
				pensum = ObservedPensum(
					careerName = response.careerName,
					selection = response.toSelection(),
					availablePensums = response.toAvailablePensums(),
					availableModalities = response.toAvailableModalities(),
					pensum = graph,
					pensums = graphs,
					approvedCredits = progress.approvedCredits,
					nodeStatuses = progress.nodeStatuses,
					nodeFulfillments = progress.nodeFulfillments
				)
			)
		}
	}

	// forceRemote is an explicit refresh (pull, retry, or nothing cached yet). Otherwise the cached
	// pensum is kept while younger than COOLDOWN_GET_PENSUM, and a recent failure holds the retry
	// off; both are re-read inside the lock, so a second caller queued behind a fetch skips it.
	override suspend fun refreshPensum(forceRemote: Boolean) {
		remoteWriteMutex.withLock {
			val cached = localDataRepository.getSelectedPensumCacheState()
			val now = currentTimeMillis()
			val isFresh = cached != null && isWithinWindow(
				sinceMillis = cached.updatedAt,
				nowMillis = now,
				windowMillis = CooldownTimes.COOLDOWN_GET_PENSUM
			)
			val isRetryOnHold = lastRevalidationFailureAt?.let { failedAt ->
				isWithinWindow(
					sinceMillis = failedAt,
					nowMillis = now,
					windowMillis = CooldownTimes.COOLDOWN_RETRY_PENSUM
				)
			} == true

			if (!forceRemote && (isFresh || isRetryOnHold)) return@withLock

			runCatching { fetchAndSave(cached = cached) }
				.onSuccess { lastRevalidationFailureAt = null }
				.onFailure { throwable ->
					if (throwable !is CancellationException) lastRevalidationFailureAt = now
					throw throwable
				}
		}
	}

	override suspend fun revalidateSelectedPensum() {
		if (!localDataRepository.hasSelectedPensumResponse()) return

		refreshPensum(forceRemote = false)
	}

	override suspend fun hasSelectedPensumResponse(): Boolean =
		localDataRepository.hasSelectedPensumResponse()

	override suspend fun selectPensum(year: Int) {
		applySelection(
			selection = localDataRepository.getSelectionParams().copy(year = year)
		) {
			localDataRepository.selectPensum(year = year)
		}
	}

	override suspend fun selectModality(modalityId: String) {
		applySelection(
			selection = localDataRepository.getSelectionParams().copy(modalityId = modalityId)
		) {
			localDataRepository.selectModality(modalityId)
		}
	}

	override suspend fun selectSelection(year: Int, modalityId: String) {
		applySelection(
			selection = PensumSelectionParams(
				year = year,
				modalityId = modalityId
			)
		) {
			localDataRepository.selectSelection(
				year = year,
				modalityId = modalityId
			)
		}
	}

	// An inferred selection is revalidated as the same year and modality, still marked inferred:
	// letting the backend infer again could move the student to another pensum on a routine
	// revalidation. Only if that pensum is gone does it fall back to a fresh inference.
	private suspend fun fetchAndSave(cached: SelectedPensumCacheState?) {
		val pinnedInferredSelection = cached
			?.takeIf { state -> state.inferred && state.year != null && state.modalityId != null }
			?.let { state -> PensumSelectionParams(year = state.year, modalityId = state.modalityId) }

		if (pinnedInferredSelection == null) {
			val selection = localDataRepository.getSelectionParams()
			localDataRepository.savePensumResponse(
				response = remoteDataRepository.getPensum(selection),
				inferredSelection = selection.year == null && selection.modalityId == null
			)
		} else {
			localDataRepository.savePensumResponse(
				response = getPensumOrInfer(pinnedInferredSelection),
				inferredSelection = true
			)
		}
	}

	private suspend fun getPensumOrInfer(selection: PensumSelectionParams): GetPensumResponse {
		return runCatching { remoteDataRepository.getPensum(selection) }
			.getOrElse { throwable ->
				if (throwable is CancellationException || !throwable.isNotFound()) throw throwable

				remoteDataRepository.getPensum(PensumSelectionParams())
			}
	}

	// The candidate selection is validated against the backend before persisting,
	// so a not-found selection never becomes the sticky default.
	private suspend fun applySelection(
		selection: PensumSelectionParams,
		persistSelection: suspend () -> Unit
	) {
		remoteWriteMutex.withLock {
			val response = remoteDataRepository.getPensum(selection)

			persistSelection()
			localDataRepository.savePensumResponse(
				response = response,
				inferredSelection = selection.year == null && selection.modalityId == null
			)
			lastRevalidationFailureAt = null
		}
	}
}
