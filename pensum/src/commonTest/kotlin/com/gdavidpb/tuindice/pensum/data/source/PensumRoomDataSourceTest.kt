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

	private fun createDataSource(
		pensumCacheDao: InMemoryPensumCacheDao,
		selectionDao: InMemoryPensumSelectionDao
	): PensumRoomDataSource {
		return PensumRoomDataSource(
			pensumCacheDao = pensumCacheDao,
			pensumSelectionDao = selectionDao,
			subjectCatalogCacheDao = InMemorySubjectCatalogCacheDao(),
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

private class InMemorySubjectCatalogCacheDao : SubjectCatalogCacheDao() {
	override fun observeSearch(normalizedQuery: String, limit: Int): Flow<List<SubjectCatalogCacheEntity>> {
		return emptyFlow()
	}

	override suspend fun upsertEntity(entity: SubjectCatalogCacheEntity) = Unit

	override suspend fun upsertEntities(entities: List<SubjectCatalogCacheEntity>) = Unit

	override suspend fun deleteAll(): Int = 0
}

private class EmptyVisibleAcademicRecordRepository : VisibleAcademicRecordRepository {
	override fun observeVisibleAcademicRecordFlow(): Flow<AcademicRecord?> = flowOf(null)
}

private class DirectTransactionRunner : PersistenceTransactionRunner {
	override suspend fun <R> immediate(block: suspend () -> R): R = block()
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
