package com.gdavidpb.tuindice.pensum.data.source

import com.gdavidpb.tuindice.base.domain.model.RecordDataPrerequisiteState
import com.gdavidpb.tuindice.base.domain.repository.RecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.pensum.data.model.GetPensumResponse
import com.gdavidpb.tuindice.pensum.data.repository.PensumLocalDataRepository
import com.gdavidpb.tuindice.pensum.data.repository.PensumRemoteDataRepository
import com.gdavidpb.tuindice.pensum.domain.engine.PensumStatusEngine
import com.gdavidpb.tuindice.pensum.domain.model.AcademicPensumSnapshot
import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.pensum.domain.model.PensumSelectionParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PensumDataSourceTest {
	@Test
	fun refreshPensum_withEmptySelection_savesResponseAsInferredSelection() = runTest {
		val local = FakePensumLocalDataRepository(selection = PensumSelectionParams())
		val remote = FakePensumRemoteDataRepository()
		val dataSource = createDataSource(local = local, remote = remote)

		dataSource.refreshPensum()

		assertEquals(PensumSelectionParams(), remote.lastSelection)
		assertEquals(true, local.lastInferredSelection)
	}

	@Test
	fun refreshPensum_withManualSelection_savesResponseAsExplicitSelection() = runTest {
		val selection = PensumSelectionParams(year = 2019, modalityId = "degree_project")
		val local = FakePensumLocalDataRepository(selection = selection)
		val remote = FakePensumRemoteDataRepository()
		val dataSource = createDataSource(local = local, remote = remote)

		dataSource.refreshPensum()

		assertEquals(selection, remote.lastSelection)
		assertEquals(false, local.lastInferredSelection)
	}

	private fun createDataSource(
		local: FakePensumLocalDataRepository,
		remote: FakePensumRemoteDataRepository
	): PensumDataSource {
		return PensumDataSource(
			localDataRepository = local,
			remoteDataRepository = remote,
			pensumStatusEngine = PensumStatusEngine(),
			recordDataPrerequisiteRepository = FakeRecordDataPrerequisiteRepository()
		)
	}
}

private class FakePensumLocalDataRepository(
	private val selection: PensumSelectionParams
) : PensumLocalDataRepository {
	var lastInferredSelection: Boolean? = null

	override fun observePensumResponseFlow(): Flow<GetPensumResponse?> = emptyFlow()

	override fun observeAcademicSnapshotFlow(): Flow<AcademicPensumSnapshot> = emptyFlow()

	override suspend fun hasSelectedPensumResponse(): Boolean = false

	override suspend fun getSelectionParams(): PensumSelectionParams = selection

	override suspend fun savePensumResponse(response: GetPensumResponse, inferredSelection: Boolean) {
		lastInferredSelection = inferredSelection
	}

	override suspend fun selectPensum(year: Int) = Unit

	override suspend fun selectModality(modalityId: String) = Unit

	override suspend fun selectSelection(year: Int, modalityId: String) = Unit
}

private class FakePensumRemoteDataRepository : PensumRemoteDataRepository {
	var lastSelection: PensumSelectionParams? = null

	override suspend fun getPensum(selection: PensumSelectionParams): GetPensumResponse {
		lastSelection = selection
		return sampleResponse()
	}
}

private class FakeRecordDataPrerequisiteRepository : RecordDataPrerequisiteRepository {
	override fun observeRecordDataPrerequisiteFlow(): Flow<RecordDataPrerequisiteState> {
		return flowOf(RecordDataPrerequisiteState(isReady = true, hasFailed = false))
	}

	override suspend fun isRecordDataReady(): Boolean = true
}

private fun sampleResponse(): GetPensumResponse {
	return GetPensumResponse(
		careerName = "Computacion",
		selectedPensumId = "computacion-2019-degree-project",
		inferred = true,
		availablePensums = listOf(GetPensumResponse.AvailablePensum(year = 2019)),
		availableModalities = listOf(
			GetPensumResponse.AvailableModality(
				id = "degree_project",
				name = "Proyecto de Grado",
				isDefault = true
			)
		),
		pensum = GetPensumResponse.Pensum(
			id = "computacion-2019-degree-project",
			year = 2019,
			modalityId = "degree_project",
			modalityName = "Proyecto de Grado",
			totalCredits = 170,
			canvas = GetPensumResponse.Canvas(width = 1200.0, height = 900.0),
			terms = emptyList(),
			nodes = emptyList(),
			edges = emptyList()
		)
	)
}
