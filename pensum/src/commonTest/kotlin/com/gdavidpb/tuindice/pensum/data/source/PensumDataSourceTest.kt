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
import kotlin.test.assertFailsWith

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

	@Test
	fun selectSelection_validatesAgainstRemoteBeforePersisting() = runTest {
		val local = FakePensumLocalDataRepository(selection = PensumSelectionParams())
		val remote = FakePensumRemoteDataRepository()
		val dataSource = createDataSource(local = local, remote = remote)

		dataSource.selectSelection(year = 2019, modalityId = "degree_project")

		assertEquals(
			PensumSelectionParams(year = 2019, modalityId = "degree_project"),
			remote.lastSelection
		)
		assertEquals(2019 to "degree_project", local.lastSelectedSelection)
		assertEquals(false, local.lastInferredSelection)
	}

	@Test
	fun selectSelection_whenRemoteFails_persistsNothing() = runTest {
		val local = FakePensumLocalDataRepository(selection = PensumSelectionParams())
		val remote = FakePensumRemoteDataRepository(
			throwable = IllegalStateException("pensum not found")
		)
		val dataSource = createDataSource(local = local, remote = remote)

		assertFailsWith<IllegalStateException> {
			dataSource.selectSelection(year = 1999, modalityId = "missing")
		}

		assertEquals(null, local.lastSelectedSelection)
		assertEquals(null, local.lastInferredSelection)
	}

	@Test
	fun selectPensum_whenRemoteFails_persistsNothing() = runTest {
		val local = FakePensumLocalDataRepository(
			selection = PensumSelectionParams(year = 2019, modalityId = "degree_project")
		)
		val remote = FakePensumRemoteDataRepository(
			throwable = IllegalStateException("pensum not found")
		)
		val dataSource = createDataSource(local = local, remote = remote)

		assertFailsWith<IllegalStateException> {
			dataSource.selectPensum(year = 1999)
		}

		assertEquals(
			PensumSelectionParams(year = 1999, modalityId = "degree_project"),
			remote.lastSelection
		)
		assertEquals(null, local.lastSelectedPensumYear)
		assertEquals(null, local.lastInferredSelection)
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
	var lastSelectedPensumYear: Int? = null
	var lastSelectedModalityId: String? = null
	var lastSelectedSelection: Pair<Int, String>? = null

	override fun observePensumResponseFlow(): Flow<GetPensumResponse?> = emptyFlow()

	override fun observeAcademicSnapshotFlow(): Flow<AcademicPensumSnapshot> = emptyFlow()

	override suspend fun hasSelectedPensumResponse(): Boolean = false

	override suspend fun getSelectionParams(): PensumSelectionParams = selection

	override suspend fun savePensumResponse(response: GetPensumResponse, inferredSelection: Boolean) {
		lastInferredSelection = inferredSelection
	}

	override suspend fun selectPensum(year: Int) {
		lastSelectedPensumYear = year
	}

	override suspend fun selectModality(modalityId: String) {
		lastSelectedModalityId = modalityId
	}

	override suspend fun selectSelection(year: Int, modalityId: String) {
		lastSelectedSelection = year to modalityId
	}
}

private class FakePensumRemoteDataRepository(
	private val throwable: Throwable? = null
) : PensumRemoteDataRepository {
	var lastSelection: PensumSelectionParams? = null

	override suspend fun getPensum(selection: PensumSelectionParams): GetPensumResponse {
		lastSelection = selection
		throwable?.let { throw it }
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
