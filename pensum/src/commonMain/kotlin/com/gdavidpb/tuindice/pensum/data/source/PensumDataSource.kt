package com.gdavidpb.tuindice.pensum.data.source

import com.gdavidpb.tuindice.base.domain.repository.RecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.pensum.data.mapper.toAvailableModalities
import com.gdavidpb.tuindice.pensum.data.mapper.toAvailablePensums
import com.gdavidpb.tuindice.pensum.data.mapper.toGraph
import com.gdavidpb.tuindice.pensum.data.mapper.toGraphs
import com.gdavidpb.tuindice.pensum.data.mapper.toSelection
import com.gdavidpb.tuindice.pensum.data.repository.PensumLocalDataRepository
import com.gdavidpb.tuindice.pensum.data.repository.PensumRemoteDataRepository
import com.gdavidpb.tuindice.pensum.domain.engine.PensumStatusEngine
import com.gdavidpb.tuindice.pensum.domain.model.ObservedPensum
import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.pensum.domain.model.PensumSelectionParams
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class PensumDataSource(
	private val localDataRepository: PensumLocalDataRepository,
	private val remoteDataRepository: PensumRemoteDataRepository,
	private val pensumStatusEngine: PensumStatusEngine,
	private val recordDataPrerequisiteRepository: RecordDataPrerequisiteRepository
) : PensumRepository {
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

	override suspend fun refreshPensum() {
		val selection = localDataRepository.getSelectionParams()
		localDataRepository.savePensumResponse(
			response = remoteDataRepository.getPensum(selection),
			inferredSelection = selection.year == null && selection.modalityId == null
		)
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

	// The candidate selection is validated against the backend before persisting,
	// so a not-found selection never becomes the sticky default.
	private suspend fun applySelection(
		selection: PensumSelectionParams,
		persistSelection: suspend () -> Unit
	) {
		val response = remoteDataRepository.getPensum(selection)

		persistSelection()
		localDataRepository.savePensumResponse(
			response = response,
			inferredSelection = selection.year == null && selection.modalityId == null
		)
	}
}
