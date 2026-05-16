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
					selection = response.toSelection(),
					availablePensums = response.toAvailablePensums(),
					availableModalities = response.toAvailableModalities(),
					pensum = graph,
					pensums = graphs,
					approvedCredits = progress.approvedCredits,
					nodeStatuses = progress.nodeStatuses
				)
			)
		}
	}

	override suspend fun refreshPensum() {
		localDataRepository.savePensumResponse(
			remoteDataRepository.getPensum(localDataRepository.getSelectionParams())
		)
	}

	override suspend fun selectPensum(careerCode: Int, year: Int) {
		localDataRepository.selectPensum(careerCode = careerCode, year = year)
		refreshPensum()
	}

	override suspend fun selectModality(modalityId: String) {
		localDataRepository.selectModality(modalityId)
		refreshPensum()
	}

	override suspend fun selectSelection(careerCode: Int, year: Int, modalityId: String) {
		localDataRepository.selectSelection(
			careerCode = careerCode,
			year = year,
			modalityId = modalityId
		)
		refreshPensum()
	}
}
