package com.gdavidpb.tuindice.pensum.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.pensum.data.model.GetPensumResponse
import com.gdavidpb.tuindice.pensum.data.model.SelectedPensumCacheState
import com.gdavidpb.tuindice.persistence.data.room.daos.PensumCacheDao
import com.gdavidpb.tuindice.persistence.data.room.daos.PensumSelectionDao
import com.gdavidpb.tuindice.persistence.data.room.daos.SubjectCatalogCacheDao
import com.gdavidpb.tuindice.persistence.data.room.entity.PensumCacheEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.PensumSelectionEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectCatalogCacheEntity
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceTransactionRunner
import com.gdavidpb.tuindice.persistence.domain.repository.VisibleAcademicRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PensumRoomDataSourceTest {
	@Test
	fun getSelectedPensumCacheState_stampsWithTheCacheRow_notWithTheSelection() = runTest {
		val threeDaysAgo = currentTimeMillis() - 3 * DAY
		val pensumCacheDao = InMemoryPensumCacheDao(
			PensumCacheEntity(
				cacheKey = "0800-2019-degree_project",
				year = 2019,
				modalityId = "degree_project",
				payloadJson = "{}",
				updatedAt = threeDaysAgo
			)
		)
		// A modality switch rewrote the selection just now without fetching anything.
		val selectionDao = InMemoryPensumSelectionDao(
			PensumSelectionEntity(
				year = 2019,
				modalityId = "degree_project",
				inferred = true,
				cacheKey = "0800-2019-degree_project",
				updatedAt = currentTimeMillis()
			)
		)

		val state = createDataSource(pensumCacheDao, selectionDao).getSelectedPensumCacheState()

		assertEquals(
			SelectedPensumCacheState(year = 2019, modalityId = "degree_project", inferred = true, updatedAt = threeDaysAgo),
			state
		)
	}

	@Test
	fun getSelectedPensumCacheState_isNullUntilTheSelectionPointsAtACachedPensum() = runTest {
		val noSelection = createDataSource(InMemoryPensumCacheDao(), InMemoryPensumSelectionDao(null))
		val selectionWithoutCacheKey = createDataSource(
			InMemoryPensumCacheDao(),
			InMemoryPensumSelectionDao(PensumSelectionEntity(year = 2019, cacheKey = null, updatedAt = 0L))
		)
		val cacheRowGone = createDataSource(
			InMemoryPensumCacheDao(),
			InMemoryPensumSelectionDao(PensumSelectionEntity(year = 2019, cacheKey = "missing", updatedAt = 0L))
		)

		assertNull(noSelection.getSelectedPensumCacheState())
		assertNull(selectionWithoutCacheKey.getSelectedPensumCacheState())
		assertNull(cacheRowGone.getSelectedPensumCacheState())
	}

	@Test
	fun savePensumResponse_leavesAFreshlyStampedSelectedPensum() = runTest {
		val before = currentTimeMillis()
		val pensumCacheDao = InMemoryPensumCacheDao()
		val selectionDao = InMemoryPensumSelectionDao(null)
		val dataSource = createDataSource(pensumCacheDao, selectionDao)

		dataSource.savePensumResponse(response = sampleResponse(), inferredSelection = true)
		val state = assertNotNull(dataSource.getSelectedPensumCacheState())

		assertEquals(2019, state.year)
		assertEquals("degree_project", state.modalityId)
		assertEquals(true, state.inferred)
		assertTrue(state.updatedAt >= before, "expected a stamp taken on save, got ${state.updatedAt} < $before")
	}

	@Test
	fun savePensumResponse_keepsTheGradingModeTheSubjectsApiStored() = runTest {
		// The subjects API stored EP3308 as pass/fail; the pensum knows no grading mode.
		val catalogDao = InMemorySubjectCatalogCacheDao(
			catalogRow(subjectCode = "EP3308", name = "PROYECTO III", credits = 2, gradingMode = "QUALITATIVE_PASS_FAIL")
		)
		val dataSource = createDataSource(InMemoryPensumCacheDao(), InMemoryPensumSelectionDao(null), catalogDao)

		dataSource.savePensumResponse(
			response = sampleResponse(
				nodes = listOf(
					courseNode(subjectCode = "EP3308", name = "PROYECTO DE GRADO III", credits = 3),
					courseNode(subjectCode = "CI2525", name = "ESTRUCTURAS DISCRETAS I", credits = 4)
				)
			),
			inferredSelection = false
		)

		val kept = catalogDao.rows.getValue("EP3308")
		assertEquals("QUALITATIVE_PASS_FAIL", kept.gradingMode)
		// Only the grading mode is kept: name and credits still follow the pensum.
		assertEquals("PROYECTO DE GRADO III", kept.name)
		assertEquals(3, kept.credits)
		// A subject the API never described is still added, with no grading mode to keep.
		assertNull(catalogDao.rows.getValue("CI2525").gradingMode)
	}

	private fun createDataSource(
		pensumCacheDao: InMemoryPensumCacheDao,
		selectionDao: InMemoryPensumSelectionDao,
		subjectCatalogCacheDao: InMemorySubjectCatalogCacheDao = InMemorySubjectCatalogCacheDao()
	): PensumRoomDataSource {
		return PensumRoomDataSource(
			pensumCacheDao = pensumCacheDao,
			pensumSelectionDao = selectionDao,
			subjectCatalogCacheDao = subjectCatalogCacheDao,
			visibleAcademicRecordRepository = EmptyVisibleAcademicRecordRepository(),
			transactionRunner = DirectTransactionRunner(),
			json = Json { ignoreUnknownKeys = true }
		)
	}
}

private const val DAY = 24L * 60L * 60L * 1000L

private class InMemoryPensumCacheDao(vararg initial: PensumCacheEntity) : PensumCacheDao() {
	private val rows = initial.associateBy(PensumCacheEntity::cacheKey).toMutableMap()

	override fun observePensum(cacheKey: String): Flow<PensumCacheEntity?> = flowOf(rows[cacheKey])

	override suspend fun getPensum(cacheKey: String): PensumCacheEntity? = rows[cacheKey]

	override suspend fun getPensum(year: Int, modalityId: String): PensumCacheEntity? {
		return rows.values.firstOrNull { row -> row.year == year && row.modalityId == modalityId }
	}

	override suspend fun upsertEntity(entity: PensumCacheEntity) {
		rows[entity.cacheKey] = entity
	}

	override suspend fun upsertEntities(entities: List<PensumCacheEntity>) {
		entities.forEach { entity -> rows[entity.cacheKey] = entity }
	}

	override suspend fun deleteAll(): Int = rows.size.also { rows.clear() }
}

private class InMemoryPensumSelectionDao(private var selection: PensumSelectionEntity?) : PensumSelectionDao() {
	override fun observeSelection(id: String): Flow<PensumSelectionEntity?> = flowOf(selection)

	override suspend fun getSelection(id: String): PensumSelectionEntity? = selection

	override suspend fun upsertEntity(entity: PensumSelectionEntity) {
		selection = entity
	}

	override suspend fun upsertEntities(entities: List<PensumSelectionEntity>) {
		entities.lastOrNull()?.let { entity -> selection = entity }
	}

	override suspend fun deleteAll(): Int = (if (selection == null) 0 else 1).also { selection = null }
}

private class InMemorySubjectCatalogCacheDao(vararg initial: SubjectCatalogCacheEntity) : SubjectCatalogCacheDao() {
	val rows = initial.associateBy(SubjectCatalogCacheEntity::subjectCode).toMutableMap()

	override fun observeSearch(normalizedQuery: String, limit: Int): Flow<List<SubjectCatalogCacheEntity>> {
		return emptyFlow()
	}

	override suspend fun getEntities(subjectCodes: List<String>): List<SubjectCatalogCacheEntity> {
		return subjectCodes.mapNotNull(rows::get)
	}

	override suspend fun upsertEntity(entity: SubjectCatalogCacheEntity) {
		rows[entity.subjectCode] = entity
	}

	override suspend fun upsertEntities(entities: List<SubjectCatalogCacheEntity>) {
		entities.forEach { entity -> rows[entity.subjectCode] = entity }
	}

	override suspend fun deleteAll(): Int = rows.size.also { rows.clear() }
}

private class EmptyVisibleAcademicRecordRepository : VisibleAcademicRecordRepository {
	override fun observeVisibleAcademicRecordFlow(): Flow<AcademicRecord?> = flowOf(null)
}

private class DirectTransactionRunner : PersistenceTransactionRunner {
	override suspend fun <R> immediate(block: suspend () -> R): R = block()
}

private fun sampleResponse(nodes: List<GetPensumResponse.Node> = emptyList()): GetPensumResponse {
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
			nodes = nodes,
			edges = emptyList()
		)
	)
}

private fun courseNode(subjectCode: String, name: String, credits: Int): GetPensumResponse.Node {
	return GetPensumResponse.Node(
		id = subjectCode.lowercase(),
		nodeType = "COURSE",
		displayCode = subjectCode,
		subjectCode = subjectCode,
		name = name,
		credits = credits,
		category = "CORE",
		termId = "term-1",
		x = 0.0,
		y = 0.0,
		width = 120.0,
		height = 60.0
	)
}

private fun catalogRow(
	subjectCode: String,
	name: String,
	credits: Int,
	gradingMode: String?
): SubjectCatalogCacheEntity {
	return SubjectCatalogCacheEntity(
		subjectCode = subjectCode,
		name = name,
		credits = credits,
		gradingMode = gradingMode,
		normalizedCode = subjectCode.lowercase(),
		normalizedName = name.lowercase(),
		updatedAt = 0L
	)
}
