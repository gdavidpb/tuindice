package com.gdavidpb.tuindice.pensum.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumSnapshot
import com.gdavidpb.tuindice.base.domain.model.RecordDataPrerequisiteState
import com.gdavidpb.tuindice.base.domain.repository.RecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.pensum.data.model.GetPensumResponse
import com.gdavidpb.tuindice.pensum.data.model.SelectedPensumCacheState
import com.gdavidpb.tuindice.pensum.data.repository.PensumLocalDataRepository
import com.gdavidpb.tuindice.pensum.data.repository.PensumRemoteDataRepository
import com.gdavidpb.tuindice.pensum.domain.engine.PensumStatusEngine
import com.gdavidpb.tuindice.pensum.domain.model.PensumSelectionParams
import com.gdavidpb.tuindice.pensum.utils.CooldownTimes
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
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

		dataSource.refreshPensum(forceRemote = true)

		assertEquals(listOf(PensumSelectionParams()), remote.requestedSelections)
		assertEquals(true, local.lastInferredSelection)
	}

	@Test
	fun refreshPensum_withManualSelection_savesResponseAsExplicitSelection() = runTest {
		val selection = PensumSelectionParams(year = 2019, modalityId = "degree_project")
		val local = FakePensumLocalDataRepository(selection = selection)
		val remote = FakePensumRemoteDataRepository()
		val dataSource = createDataSource(local = local, remote = remote)

		dataSource.refreshPensum(forceRemote = true)

		assertEquals(listOf(selection), remote.requestedSelections)
		assertEquals(false, local.lastInferredSelection)
	}

	@Test
	fun refreshPensum_keepsAPensumYoungerThanADay_unlessForced() = runTest {
		val selection = PensumSelectionParams(year = 2019, modalityId = "degree_project")
		val local = FakePensumLocalDataRepository(
			selection = selection,
			cacheState = cacheState(ageMillis = CooldownTimes.COOLDOWN_GET_PENSUM - HOUR)
		)
		val remote = FakePensumRemoteDataRepository()
		val dataSource = createDataSource(local = local, remote = remote)

		dataSource.refreshPensum(forceRemote = false)
		assertEquals(emptyList(), remote.requestedSelections)

		dataSource.refreshPensum(forceRemote = true)
		assertEquals(listOf(selection), remote.requestedSelections)
	}

	@Test
	fun refreshPensum_revalidatesAPensumOlderThanADay() = runTest {
		val selection = PensumSelectionParams(year = 2019, modalityId = "degree_project")
		val local = FakePensumLocalDataRepository(
			selection = selection,
			cacheState = cacheState(ageMillis = CooldownTimes.COOLDOWN_GET_PENSUM + HOUR)
		)
		val remote = FakePensumRemoteDataRepository()
		val dataSource = createDataSource(local = local, remote = remote)

		dataSource.refreshPensum(forceRemote = false)

		assertEquals(listOf(selection), remote.requestedSelections)
		assertEquals(false, local.lastInferredSelection)
	}

	@Test
	fun refreshPensum_afterAFailedRevalidation_holdsTheNextAutomaticOneOff_butNotAForcedOne() = runTest {
		val selection = PensumSelectionParams(year = 2019, modalityId = "degree_project")
		val local = FakePensumLocalDataRepository(
			selection = selection,
			cacheState = cacheState(ageMillis = CooldownTimes.COOLDOWN_GET_PENSUM + HOUR)
		)
		val remote = FakePensumRemoteDataRepository(throwable = IllegalStateException("offline"))
		val dataSource = createDataSource(local = local, remote = remote)

		assertFailsWith<IllegalStateException> { dataSource.refreshPensum(forceRemote = false) }
		dataSource.refreshPensum(forceRemote = false)
		assertEquals(1, remote.requestedSelections.size)

		remote.throwable = null
		dataSource.refreshPensum(forceRemote = true)
		assertEquals(2, remote.requestedSelections.size)
	}

	@Test
	fun refreshPensum_forAnInferredSelection_revalidatesTheSamePensum_andKeepsItInferred() = runTest {
		// Inferred selections read back as empty params: only the cache row knows which pensum it is.
		val local = FakePensumLocalDataRepository(
			selection = PensumSelectionParams(),
			cacheState = cacheState(ageMillis = CooldownTimes.COOLDOWN_GET_PENSUM + HOUR, inferred = true)
		)
		val remote = FakePensumRemoteDataRepository()
		val dataSource = createDataSource(local = local, remote = remote)

		dataSource.refreshPensum(forceRemote = false)

		assertEquals(
			listOf(PensumSelectionParams(year = 2019, modalityId = "degree_project")),
			remote.requestedSelections
		)
		assertEquals(true, local.lastInferredSelection)
	}

	@Test
	fun refreshPensum_forAnInferredPensumThatIsGone_infersAgain() = runTest {
		val pinned = PensumSelectionParams(year = 2019, modalityId = "degree_project")
		val local = FakePensumLocalDataRepository(
			selection = PensumSelectionParams(),
			cacheState = cacheState(ageMillis = CooldownTimes.COOLDOWN_GET_PENSUM + HOUR, inferred = true)
		)
		val remote = FakePensumRemoteDataRepository(
			throwableBySelection = mapOf(pinned to clientRequestException(HttpStatusCode.NotFound))
		)
		val dataSource = createDataSource(local = local, remote = remote)

		dataSource.refreshPensum(forceRemote = false)

		assertEquals(listOf(pinned, PensumSelectionParams()), remote.requestedSelections)
		assertEquals(true, local.lastInferredSelection)
	}

	@Test
	fun refreshPensum_forAnInferredPensum_propagatesAnyFailureButNotFound() = runTest {
		val pinned = PensumSelectionParams(year = 2019, modalityId = "degree_project")
		val local = FakePensumLocalDataRepository(
			selection = PensumSelectionParams(),
			cacheState = cacheState(ageMillis = CooldownTimes.COOLDOWN_GET_PENSUM + HOUR, inferred = true)
		)
		val remote = FakePensumRemoteDataRepository(
			throwableBySelection = mapOf(pinned to clientRequestException(HttpStatusCode.ServiceUnavailable))
		)
		val dataSource = createDataSource(local = local, remote = remote)

		assertFailsWith<Exception> { dataSource.refreshPensum(forceRemote = false) }

		assertEquals(listOf(pinned), remote.requestedSelections)
		assertEquals(null, local.lastInferredSelection)
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun selection_madeDuringARevalidation_isPersistedAfterIt_neverOverwrittenByIt() = runTest {
		val local = FakePensumLocalDataRepository(
			selection = PensumSelectionParams(),
			cacheState = cacheState(ageMillis = CooldownTimes.COOLDOWN_GET_PENSUM + HOUR, inferred = true)
		)
		val gate = CompletableDeferred<Unit>()
		val remote = FakePensumRemoteDataRepository(firstRequestGate = gate)
		val dataSource = createDataSource(local = local, remote = remote)

		val revalidation = launch { dataSource.refreshPensum(forceRemote = false) }
		runCurrent()
		val selection = launch { dataSource.selectSelection(year = 2018, modalityId = "long_internship") }
		runCurrent()

		// The manual selection waits for the in-flight revalidation instead of racing it.
		assertEquals(emptyList(), local.writes)

		gate.complete(Unit)
		revalidation.join()
		selection.join()

		assertEquals(
			listOf("save inferred=true", "select 2018/long_internship", "save inferred=false"),
			local.writes
		)
	}

	@Test
	fun revalidateSelectedPensum_withoutACachedPensum_fetchesNothing() = runTest {
		val local = FakePensumLocalDataRepository(selection = PensumSelectionParams(), cacheState = null)
		val remote = FakePensumRemoteDataRepository()
		val dataSource = createDataSource(local = local, remote = remote)

		dataSource.revalidateSelectedPensum()

		assertEquals(emptyList(), remote.requestedSelections)
	}

	@Test
	fun revalidateSelectedPensum_respectsTheAgeOfTheCachedPensum() = runTest {
		val selection = PensumSelectionParams(year = 2019, modalityId = "degree_project")
		val local = FakePensumLocalDataRepository(
			selection = selection,
			cacheState = cacheState(ageMillis = HOUR)
		)
		val remote = FakePensumRemoteDataRepository()
		val dataSource = createDataSource(local = local, remote = remote)

		dataSource.revalidateSelectedPensum()
		assertEquals(emptyList(), remote.requestedSelections)

		local.cacheState = cacheState(ageMillis = CooldownTimes.COOLDOWN_GET_PENSUM + HOUR)
		dataSource.revalidateSelectedPensum()
		assertEquals(listOf(selection), remote.requestedSelections)
	}

	@Test
	fun selectSelection_validatesAgainstRemoteBeforePersisting() = runTest {
		val local = FakePensumLocalDataRepository(selection = PensumSelectionParams())
		val remote = FakePensumRemoteDataRepository()
		val dataSource = createDataSource(local = local, remote = remote)

		dataSource.selectSelection(year = 2019, modalityId = "degree_project")

		assertEquals(
			listOf(PensumSelectionParams(year = 2019, modalityId = "degree_project")),
			remote.requestedSelections
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
			listOf(PensumSelectionParams(year = 1999, modalityId = "degree_project")),
			remote.requestedSelections
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

	private fun cacheState(ageMillis: Long, inferred: Boolean = false): SelectedPensumCacheState {
		return SelectedPensumCacheState(
			year = 2019,
			modalityId = "degree_project",
			inferred = inferred,
			updatedAt = currentTimeMillis() - ageMillis
		)
	}
}

private const val HOUR = 60L * 60L * 1000L

internal class FakePensumLocalDataRepository(
	private val selection: PensumSelectionParams,
	var cacheState: SelectedPensumCacheState? = null
) : PensumLocalDataRepository {
	var lastInferredSelection: Boolean? = null
	var lastSelectedPensumYear: Int? = null
	var lastSelectedModalityId: String? = null
	var lastSelectedSelection: Pair<Int, String>? = null
	val writes = mutableListOf<String>()

	override fun observePensumResponseFlow(): Flow<GetPensumResponse?> = emptyFlow()

	override fun observeAcademicSnapshotFlow(): Flow<AcademicPensumSnapshot> = emptyFlow()

	override suspend fun hasSelectedPensumResponse(): Boolean = cacheState != null

	override suspend fun getSelectedPensumCacheState(): SelectedPensumCacheState? = cacheState

	override suspend fun getSelectionParams(): PensumSelectionParams = selection

	override suspend fun savePensumResponse(response: GetPensumResponse, inferredSelection: Boolean) {
		lastInferredSelection = inferredSelection
		writes += "save inferred=$inferredSelection"
	}

	override suspend fun selectPensum(year: Int) {
		lastSelectedPensumYear = year
		writes += "select $year"
	}

	override suspend fun selectModality(modalityId: String) {
		lastSelectedModalityId = modalityId
		writes += "select modality $modalityId"
	}

	override suspend fun selectSelection(year: Int, modalityId: String) {
		lastSelectedSelection = year to modalityId
		writes += "select $year/$modalityId"
	}
}

internal class FakePensumRemoteDataRepository(
	var throwable: Throwable? = null,
	private val throwableBySelection: Map<PensumSelectionParams, Throwable> = emptyMap(),
	// Holds only the first request, so a later one can overtake it: a shared gate would hold every
	// caller and hide the very race the lock prevents.
	private val firstRequestGate: CompletableDeferred<Unit>? = null
) : PensumRemoteDataRepository {
	val requestedSelections = mutableListOf<PensumSelectionParams>()

	override suspend fun getPensum(selection: PensumSelectionParams): GetPensumResponse {
		requestedSelections += selection
		if (requestedSelections.size == 1) firstRequestGate?.await()
		throwableBySelection[selection]?.let { throw it }
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
