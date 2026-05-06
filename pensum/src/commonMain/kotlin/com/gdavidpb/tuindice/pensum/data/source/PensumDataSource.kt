package com.gdavidpb.tuindice.pensum.data.source

import com.gdavidpb.tuindice.pensum.data.mapper.toAvailableModalities
import com.gdavidpb.tuindice.pensum.data.mapper.toAvailablePensums
import com.gdavidpb.tuindice.pensum.data.mapper.toGraph
import com.gdavidpb.tuindice.pensum.data.mapper.toSelection
import com.gdavidpb.tuindice.pensum.data.repository.PensumLocalDataRepository
import com.gdavidpb.tuindice.pensum.data.repository.PensumRemoteDataRepository
import com.gdavidpb.tuindice.pensum.domain.engine.PensumStatusEngine
import com.gdavidpb.tuindice.pensum.domain.model.ObservedPensum
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class PensumDataSource(
	private val localDataRepository: PensumLocalDataRepository,
	private val remoteDataRepository: PensumRemoteDataRepository,
	private val pensumStatusEngine: PensumStatusEngine
) : PensumRepository {
	override fun observePensumFlow(): Flow<ObservedPensum?> {
		return combine(
			localDataRepository.observePensumResponseFlow(),
			localDataRepository.observeAcademicSnapshotFlow()
		) { response, academicSnapshot ->
			response ?: return@combine null
			val graph = response.toGraph()
			val progress = pensumStatusEngine.resolve(
				pensum = graph,
				academicSnapshot = academicSnapshot
			)

			ObservedPensum(
				selection = response.toSelection(),
				availablePensums = response.toAvailablePensums(),
				availableModalities = response.toAvailableModalities(),
				pensum = graph,
				approvedCredits = progress.approvedCredits,
				nodeStatuses = progress.nodeStatuses
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
