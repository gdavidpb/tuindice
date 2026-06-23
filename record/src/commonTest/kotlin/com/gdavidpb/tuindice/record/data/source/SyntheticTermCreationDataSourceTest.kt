@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.domain.model.GradingMode
import com.gdavidpb.tuindice.base.domain.utils.SubjectCatalogSearchNormalizer
import com.gdavidpb.tuindice.persistence.data.room.daos.PensumCacheDao
import com.gdavidpb.tuindice.persistence.data.room.daos.PensumSelectionDao
import com.gdavidpb.tuindice.persistence.data.room.daos.SubjectCatalogCacheDao
import com.gdavidpb.tuindice.persistence.data.room.entity.PensumCacheEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.PensumSelectionEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectCatalogCacheEntity
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationCommand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubjectAvailability
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermUpdateCommand
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class SyntheticTermCreationDataSourceTest {
	@Test
	fun observeSnapshot_startsPeriodOptionsAtCurrentTerm_whenLatestAcademicTermIsStale() = runTest {
		val currentPeriod = currentSyntheticTermPeriodOption()
		val dataSource = dataSource(
			record = AcademicRecord(
				id = "record",
				terms = listOf(
					academicTerm(
						id = "2015-SEP_DEC",
						kind = TermKind.HISTORICAL,
						periodYear = 2015,
						periodCode = AcademicTermPeriod.SEP_DEC,
						attempts = emptyList()
					)
				)
			),
			pensumPayloadJson = pensumPayload(nodes = emptyList(), edges = emptyList()),
			searchEntities = emptyList()
		)

		val snapshot = dataSource.observeSnapshot(
			queryFlow = MutableStateFlow(""),
			selectedSubjectsFlow = MutableStateFlow(emptyList()),
			selectedPeriodKeyFlow = MutableStateFlow(null),
			editingTermIdFlow = MutableStateFlow(null),
			editingTermKeyFlow = MutableStateFlow(null)
		).first()

		assertEquals(20, snapshot.periodOptions.size)
		assertEquals(currentPeriod, snapshot.periodOptions.first())
		assertEquals(currentPeriod, snapshot.selectedPeriod)
	}

	@Test
	fun observeSnapshot_resolvesEverySearchResultAvailabilityState() = runTest {
		val dataSource = dataSource(
			record = AcademicRecord(
				id = "record",
				terms = listOf(
					academicTerm(
						id = "historical",
						kind = TermKind.HISTORICAL,
						attempts = listOf(
							academicAttempt(subjectCode = "CC1001", outcome = AttemptOutcome.APPROVED)
						)
					),
					academicTerm(
						id = "current",
						kind = TermKind.CURRENT,
						periodCode = AcademicTermPeriod.APR_JUL,
						attempts = listOf(
							academicAttempt(subjectCode = "BB1001", outcome = AttemptOutcome.PENDING)
						)
					),
					academicTerm(
						id = "synthetic",
						kind = TermKind.SYNTHETIC,
						attempts = listOf(
							academicAttempt(subjectCode = "DD1001", outcome = AttemptOutcome.PENDING)
						)
					)
				)
			),
			pensumPayloadJson = pensumPayload(
				nodes = listOf(
					pensumNode(id = "aa1001", subjectCode = "AA1001", name = "Estado disponible"),
					pensumNode(id = "bb1001", subjectCode = "BB1001", name = "Estado en curso"),
					pensumNode(id = "cc1001", subjectCode = "CC1001", name = "Estado cursada"),
					pensumNode(id = "dd1001", subjectCode = "DD1001", name = "Estado planificada"),
					pensumNode(id = "ff1001", subjectCode = "FF1001", name = "Estado requisito faltante"),
					pensumNode(id = "ee1001", subjectCode = "EE1001", name = "Estado no disponible")
				),
				edges = listOf(
					pensumEdge(fromNodeId = "ff1001", toNodeId = "ee1001", relationshipType = "REQUIREMENT")
				)
			),
			searchEntities = listOf(
				subjectCatalogEntity(subjectCode = "AA1001", name = "Estado disponible"),
				subjectCatalogEntity(subjectCode = "BB1001", name = "Estado en curso"),
				subjectCatalogEntity(subjectCode = "CC1001", name = "Estado cursada"),
				subjectCatalogEntity(subjectCode = "DD1001", name = "Estado planificada"),
				subjectCatalogEntity(subjectCode = "EE1001", name = "Estado no disponible"),
				subjectCatalogEntity(subjectCode = "GG1001", name = "Estado fuera de pensum")
			)
		)

		val snapshot = dataSource.observeSnapshot(
			queryFlow = MutableStateFlow("estado"),
			selectedSubjectsFlow = MutableStateFlow(emptyList()),
			selectedPeriodKeyFlow = MutableStateFlow(null),
			editingTermIdFlow = MutableStateFlow(null),
			editingTermKeyFlow = MutableStateFlow(null)
		).first()
		val resultsByCode = snapshot.searchResults.associateBy { subject -> subject.subjectCode }

		assertEquals(
			listOf(
				"AA1001" to SyntheticTermSubjectAvailability.AVAILABLE,
				"DD1001" to SyntheticTermSubjectAvailability.ALREADY_PLANNED,
				"EE1001" to SyntheticTermSubjectAvailability.BLOCKED,
				"BB1001" to SyntheticTermSubjectAvailability.CURRENT,
				"CC1001" to SyntheticTermSubjectAvailability.APPROVED,
				"GG1001" to SyntheticTermSubjectAvailability.NOT_IN_PENSUM
			),
			snapshot.searchResults.map { subject -> subject.subjectCode to subject.availability }
		)
		assertEquals("Abr - Jul 2025", resultsByCode.getValue("BB1001").availabilityDetail?.termLabel)
		assertEquals("Ene - Mar 2025", resultsByCode.getValue("CC1001").availabilityDetail?.termLabel)
		assertEquals("Ene - Mar 2025", resultsByCode.getValue("DD1001").availabilityDetail?.termLabel)
		assertEquals(listOf("FF1001"), resultsByCode.getValue("EE1001").availabilityDetail?.missingSubjectCodes)
	}

	@Test
	fun observeSnapshot_marksSearchResultBlocked_whenPensumRequirementIsPending() = runTest {
		val dataSource = dataSource(
			record = academicRecord(
				approvedSubjectCodes = listOf("MA1111")
			),
			pensumPayloadJson = pensumPayload(
				nodes = listOf(
					pensumNode(id = "ma1112", subjectCode = "MA1112", name = "Matemáticas II"),
					pensumNode(id = "ec1111", subjectCode = "EC1111", name = "Circuitos")
				),
				edges = listOf(
					pensumEdge(fromNodeId = "ma1112", toNodeId = "ec1111", relationshipType = "REQUIREMENT")
				)
			),
			searchEntities = listOf(
				subjectCatalogEntity(subjectCode = "EC1111", name = "Circuitos")
			)
		)

		val snapshot = dataSource.observeSnapshot(
			queryFlow = MutableStateFlow("ec"),
			selectedSubjectsFlow = MutableStateFlow(emptyList()),
			selectedPeriodKeyFlow = MutableStateFlow(null),
			editingTermIdFlow = MutableStateFlow(null),
			editingTermKeyFlow = MutableStateFlow(null)
		).first()

		val result = snapshot.searchResults.single()
		assertEquals(SyntheticTermSubjectAvailability.BLOCKED, result.availability)
		assertEquals(listOf("MA1112"), result.availabilityDetail?.missingSubjectCodes)
		assertTrue(result.canAdd)
	}

	@Test
	fun observeSnapshot_rehydratesSelectedSubjectsWithPensumAvailability_whenEditingTerm() = runTest {
		val dataSource = dataSource(
			record = academicRecord(
				approvedSubjectCodes = listOf("MA1112")
			),
			pensumPayloadJson = pensumPayload(
				nodes = listOf(
					pensumNode(id = "ma1112", subjectCode = "MA1112", name = "Matemáticas II"),
					pensumNode(id = "ma1122", subjectCode = "MA1122", name = "Matemáticas II"),
					pensumNode(id = "ci2511", subjectCode = "CI2511", name = "Lógica Simbólica")
				),
				edges = listOf(
					pensumEdge(fromNodeId = "ma1112", toNodeId = "ci2511", relationshipType = "REQUIREMENT"),
					pensumEdge(fromNodeId = "ma1122", toNodeId = "ci2511", relationshipType = "REQUIREMENT")
				)
			),
			searchEntities = emptyList()
		)

		val snapshot = dataSource.observeSnapshot(
			queryFlow = MutableStateFlow(""),
			selectedSubjectsFlow = MutableStateFlow(
				listOf(
					SyntheticTermSubject(
						subjectCode = "CI2511",
						name = "Lógica Simbólica",
						credits = 4
					)
				)
			),
			selectedPeriodKeyFlow = MutableStateFlow(null),
			editingTermIdFlow = MutableStateFlow("synthetic-term"),
			editingTermKeyFlow = MutableStateFlow("2026-APR_JUL")
		).first()

		val selectedSubject = snapshot.selectedSubjects.single()
		assertEquals(SyntheticTermSubjectAvailability.BLOCKED, selectedSubject.availability)
		assertEquals(listOf("MA1122"), selectedSubject.availabilityDetail?.missingSubjectCodes)
	}

	@Test
	fun observeSnapshot_resolvesFailedAndRetiredHistoricalSearchResultsFromPensumAvailability() = runTest {
		val dataSource = dataSource(
			record = AcademicRecord(
				id = "record",
				terms = listOf(
					academicTerm(
						id = "historical",
						kind = TermKind.HISTORICAL,
						attempts = listOf(
							academicAttempt(subjectCode = "MA1111", outcome = AttemptOutcome.APPROVED),
							academicAttempt(subjectCode = "MA1112", outcome = AttemptOutcome.RETIRED),
							academicAttempt(subjectCode = "MA1121", outcome = AttemptOutcome.FAILED)
						)
					)
				)
			),
			pensumPayloadJson = pensumPayload(
				nodes = listOf(
					pensumNode(id = "ma1111", subjectCode = "MA1111", name = "Matemáticas I"),
					pensumNode(id = "ma1112", subjectCode = "MA1112", name = "Matemáticas II"),
					pensumNode(id = "ma1121", subjectCode = "MA1121", name = "Matemáticas I Honor"),
					pensumNode(id = "ma1116", subjectCode = "MA1116", name = "Matemáticas III")
				),
				edges = listOf(
					pensumEdge(fromNodeId = "ma1111", toNodeId = "ma1112", relationshipType = "REQUIREMENT")
				)
			),
			searchEntities = listOf(
				subjectCatalogEntity(subjectCode = "MA1111", name = "Matemáticas I"),
				subjectCatalogEntity(subjectCode = "MA1112", name = "Matemáticas II"),
				subjectCatalogEntity(subjectCode = "MA1121", name = "Matemáticas I Honor")
			)
		)

		val snapshot = dataSource.observeSnapshot(
			queryFlow = MutableStateFlow("matematicas"),
			selectedSubjectsFlow = MutableStateFlow(emptyList()),
			selectedPeriodKeyFlow = MutableStateFlow(null),
			editingTermIdFlow = MutableStateFlow(null),
			editingTermKeyFlow = MutableStateFlow(null)
		).first()

		assertEquals(
			listOf(
				"MA1112" to SyntheticTermSubjectAvailability.AVAILABLE,
				"MA1121" to SyntheticTermSubjectAvailability.AVAILABLE,
				"MA1111" to SyntheticTermSubjectAvailability.APPROVED
			),
			snapshot.searchResults.map { subject -> subject.subjectCode to subject.availability }
		)
		assertTrue(snapshot.searchResults.first { subject -> subject.subjectCode == "MA1112" }.canAdd)
		assertTrue(snapshot.searchResults.first { subject -> subject.subjectCode == "MA1121" }.canAdd)
	}

	@Test
	fun observeSnapshot_marksSearchResultNotInPensum_whenSubjectIsOnlyInCatalog() = runTest {
		val dataSource = dataSource(
			record = academicRecord(
				approvedSubjectCodes = listOf("MA1111")
			),
			pensumPayloadJson = pensumPayload(
				nodes = listOf(
					pensumNode(id = "ma1111", subjectCode = "MA1111", name = "Matemáticas I")
				),
				edges = emptyList()
			),
			searchEntities = listOf(
				subjectCatalogEntity(subjectCode = "EP2308", name = "Proyecto de Grado II")
			)
		)

		val snapshot = dataSource.observeSnapshot(
			queryFlow = MutableStateFlow("ep2308"),
			selectedSubjectsFlow = MutableStateFlow(emptyList()),
			selectedPeriodKeyFlow = MutableStateFlow(null),
			editingTermIdFlow = MutableStateFlow(null),
			editingTermKeyFlow = MutableStateFlow(null)
		).first()

		val result = snapshot.searchResults.single()
		assertEquals(SyntheticTermSubjectAvailability.NOT_IN_PENSUM, result.availability)
		assertTrue(result.canAdd)
	}

	@Test
	fun observeSnapshot_suggestsFailedAndRetiredHistoricalSubjects_whenPensumRequirementsAreMet() = runTest {
		val dataSource = dataSource(
			record = AcademicRecord(
				id = "record",
				terms = listOf(
					academicTerm(
						id = "historical",
						kind = TermKind.HISTORICAL,
						attempts = listOf(
							academicAttempt(subjectCode = "MA1111", outcome = AttemptOutcome.APPROVED),
							academicAttempt(subjectCode = "MA1112", outcome = AttemptOutcome.RETIRED),
							academicAttempt(subjectCode = "MA1121", outcome = AttemptOutcome.FAILED)
						)
					)
				)
			),
			pensumPayloadJson = pensumPayload(
				nodes = listOf(
					pensumNode(id = "ma1111", subjectCode = "MA1111", name = "Matemáticas I"),
					pensumNode(id = "ma1112", subjectCode = "MA1112", name = "Matemáticas II"),
					pensumNode(id = "ma1121", subjectCode = "MA1121", name = "Matemáticas I Honor")
				),
				edges = listOf(
					pensumEdge(fromNodeId = "ma1111", toNodeId = "ma1112", relationshipType = "REQUIREMENT")
				)
			),
			searchEntities = emptyList()
		)

		val snapshot = dataSource.observeSnapshot(
			queryFlow = MutableStateFlow(""),
			selectedSubjectsFlow = MutableStateFlow(emptyList()),
			selectedPeriodKeyFlow = MutableStateFlow(null),
			editingTermIdFlow = MutableStateFlow(null),
			editingTermKeyFlow = MutableStateFlow(null)
		).first()

		assertEquals(
			listOf("MA1112", "MA1121"),
			snapshot.suggestedSubjects.map { subject -> subject.subjectCode }
		)
	}

	@Test
	fun observeSnapshot_readsSelectedPensumFromPersistedPensumResponseCache() = runTest {
		val dataSource = dataSource(
			record = academicRecord(
				approvedSubjectCodes = listOf("MA1111")
			),
			pensumPayloadJson = pensumResponsePayload(
				selectedPensumId = "selected-pensum",
				pensums = listOf(
					pensumPayloadPensum(
						id = "other-pensum",
						nodes = listOf(
							pensumNode(id = "aa1001", subjectCode = "AA1001", name = "Otra seleccion")
						),
						edges = emptyList()
					),
					pensumPayloadPensum(
						id = "selected-pensum",
						nodes = listOf(
							pensumNode(id = "ma1111", subjectCode = "MA1111", name = "Matemáticas I"),
							pensumNode(id = "ma1112", subjectCode = "MA1112", name = "Matemáticas II")
						),
						edges = listOf(
							pensumEdge(fromNodeId = "ma1111", toNodeId = "ma1112", relationshipType = "REQUIREMENT")
						)
					)
				)
			),
			searchEntities = listOf(
				subjectCatalogEntity(subjectCode = "MA1112", name = "Matemáticas II")
			)
		)

		val snapshot = dataSource.observeSnapshot(
			queryFlow = MutableStateFlow("ma1112"),
			selectedSubjectsFlow = MutableStateFlow(emptyList()),
			selectedPeriodKeyFlow = MutableStateFlow(null),
			editingTermIdFlow = MutableStateFlow(null),
			editingTermKeyFlow = MutableStateFlow(null)
		).first()

		assertEquals(
			listOf("MA1112"),
			snapshot.suggestedSubjects.map { subject -> subject.subjectCode }
		)
		assertEquals(
			SyntheticTermSubjectAvailability.AVAILABLE,
			snapshot.searchResults.single().availability
		)
	}

	@Test
	fun observeSnapshot_marksProjectSubjectBlocked_whenRequirementsAreOnlyPlanned() = runTest {
		val dataSource = dataSource(
			record = AcademicRecord(
				id = "record",
				terms = listOf(
					academicTerm(
						id = "synthetic",
						kind = TermKind.SYNTHETIC,
						attempts = listOf(
							academicAttempt(subjectCode = "EP1308", outcome = AttemptOutcome.PENDING),
							academicAttempt(subjectCode = "EP5855", outcome = AttemptOutcome.PENDING)
						)
					)
				)
			),
			pensumPayloadJson = pensumPayload(
				nodes = listOf(
					pensumNode(id = "ep1308", subjectCode = "EP1308", name = "Proyecto de Grado I"),
					pensumNode(id = "ep5855", subjectCode = "EP5855", name = "Tópicos Especiales I"),
					pensumNode(id = "ep2308", subjectCode = "EP2308", name = "Proyecto de Grado II")
				),
				edges = listOf(
					pensumEdge(fromNodeId = "ep1308", toNodeId = "ep2308", relationshipType = "REQUIREMENT"),
					pensumEdge(fromNodeId = "ep5855", toNodeId = "ep2308", relationshipType = "REQUIREMENT")
				)
			),
			searchEntities = listOf(
				subjectCatalogEntity(subjectCode = "EP2308", name = "Proyecto de Grado II")
			)
		)

		val snapshot = dataSource.observeSnapshot(
			queryFlow = MutableStateFlow("ep2308"),
			selectedSubjectsFlow = MutableStateFlow(emptyList()),
			selectedPeriodKeyFlow = MutableStateFlow(null),
			editingTermIdFlow = MutableStateFlow(null),
			editingTermKeyFlow = MutableStateFlow(null)
		).first()

		val result = snapshot.searchResults.single()
		assertEquals(SyntheticTermSubjectAvailability.BLOCKED, result.availability)
		assertEquals(listOf("EP1308", "EP5855"), result.availabilityDetail?.missingSubjectCodes)
		assertTrue(result.canAdd)
	}

	@Test
	fun observeSnapshot_ordersPensumSearchResultsBeforeCatalogOnlyMatches() = runTest {
		val dataSource = dataSource(
			record = AcademicRecord(
				id = "record",
				terms = listOf(
					academicTerm(
						id = "synthetic",
						kind = TermKind.SYNTHETIC,
						attempts = listOf(
							academicAttempt(subjectCode = "EP1308", outcome = AttemptOutcome.PENDING)
						)
					)
				)
			),
			pensumPayloadJson = pensumPayload(
				nodes = listOf(
					pensumNode(id = "ep1308", subjectCode = "EP1308", name = "Prioridad planificada del pensum"),
					pensumNode(id = "ma1112", subjectCode = "MA1112", name = "Prioridad requisito faltante"),
					pensumNode(id = "ep2308", subjectCode = "EP2308", name = "Prioridad bloqueada del pensum")
				),
				edges = listOf(
					pensumEdge(fromNodeId = "ma1112", toNodeId = "ep2308", relationshipType = "REQUIREMENT")
				)
			),
			searchEntities = listOf(
				subjectCatalogEntity(subjectCode = "AA1001", name = "Prioridad fuera del pensum alfa"),
				subjectCatalogEntity(subjectCode = "AB1001", name = "Prioridad fuera del pensum beta"),
				subjectCatalogEntity(subjectCode = "EP2308", name = "Prioridad bloqueada del pensum"),
				subjectCatalogEntity(subjectCode = "EP1308", name = "Prioridad planificada del pensum")
			)
		)

		val snapshot = dataSource.observeSnapshot(
			queryFlow = MutableStateFlow("prioridad"),
			selectedSubjectsFlow = MutableStateFlow(emptyList()),
			selectedPeriodKeyFlow = MutableStateFlow(null),
			editingTermIdFlow = MutableStateFlow(null),
			editingTermKeyFlow = MutableStateFlow(null)
		).first()

		assertEquals(
			listOf(
				"EP1308" to SyntheticTermSubjectAvailability.ALREADY_PLANNED,
				"EP2308" to SyntheticTermSubjectAvailability.BLOCKED,
				"AA1001" to SyntheticTermSubjectAvailability.NOT_IN_PENSUM,
				"AB1001" to SyntheticTermSubjectAvailability.NOT_IN_PENSUM
			),
			snapshot.searchResults.map { subject -> subject.subjectCode to subject.availability }
		)
	}

	private fun dataSource(
		record: AcademicRecord,
		pensumPayloadJson: String,
		searchEntities: List<SubjectCatalogCacheEntity>
	): SyntheticTermCreationDataSource {
		val cacheKey = "2016-regular"
		return SyntheticTermCreationDataSource(
			academicRecordRepository = FakeAcademicRecordRepository(record),
			pensumCacheDao = FakePensumCacheDao(
				cache = PensumCacheEntity(
					cacheKey = cacheKey,
					year = 2016,
					modalityId = "regular",
					payloadJson = pensumPayloadJson,
					updatedAt = 1L
				)
			),
			pensumSelectionDao = FakePensumSelectionDao(
				selection = PensumSelectionEntity(
					year = 2016,
					modalityId = "regular",
					cacheKey = cacheKey,
					updatedAt = 1L
				)
			),
			subjectCatalogCacheDao = FakeSubjectCatalogCacheDao(searchEntities),
			ktorClient = HttpClient(MockEngine { respondOk() }),
			json = Json {
				ignoreUnknownKeys = true
			}
		)
	}
}

private class FakeAcademicRecordRepository(
	private val record: AcademicRecord
) : AcademicRecordRepository {
	override suspend fun observeAcademicRecordFlow(): Flow<AcademicRecord> = flowOf(record)

	override suspend fun observeHasSyncedRecordFlow(): Flow<Boolean> = flowOf(true)

	override suspend fun getAcademicRecord(): AcademicRecord = record

	override suspend fun updateAcademicRecord() = Unit

	override suspend fun drainPendingMutations() = Unit

	override suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: AttemptOutcome?,
		commit: Boolean
	) = Unit

	override suspend fun deleteAttemptOverride(attemptId: String) = Unit

	override suspend fun addSyntheticTerm(command: SyntheticTermCreationCommand) = Unit

	override suspend fun updateSyntheticTerm(command: SyntheticTermUpdateCommand) = Unit

	override suspend fun deleteSyntheticTerm(termId: String) = Unit
}

private class FakePensumCacheDao(
	private val cache: PensumCacheEntity
) : PensumCacheDao() {
	override fun observePensum(cacheKey: String): Flow<PensumCacheEntity?> {
		return flowOf(cache.takeIf { entity -> entity.cacheKey == cacheKey })
	}

	override suspend fun getPensum(cacheKey: String): PensumCacheEntity? {
		return cache.takeIf { entity -> entity.cacheKey == cacheKey }
	}

	override suspend fun getPensum(
		year: Int,
		modalityId: String
	): PensumCacheEntity? {
		return cache.takeIf { entity -> entity.year == year && entity.modalityId == modalityId }
	}

	override suspend fun upsertEntity(entity: PensumCacheEntity) = Unit

	override suspend fun upsertEntities(entities: List<PensumCacheEntity>) = Unit

	override suspend fun deleteAll(): Int = 0
}

private class FakePensumSelectionDao(
	private val selection: PensumSelectionEntity
) : PensumSelectionDao() {
	override fun observeSelection(id: String): Flow<PensumSelectionEntity?> = flowOf(selection)

	override suspend fun getSelection(id: String): PensumSelectionEntity = selection

	override suspend fun upsertEntity(entity: PensumSelectionEntity) = Unit

	override suspend fun upsertEntities(entities: List<PensumSelectionEntity>) = Unit

	override suspend fun deleteAll(): Int = 0
}

private class FakeSubjectCatalogCacheDao(
	private val entities: List<SubjectCatalogCacheEntity>
) : SubjectCatalogCacheDao() {
	override fun observeSearch(
		normalizedQuery: String,
		limit: Int
	): Flow<List<SubjectCatalogCacheEntity>> {
		return flowOf(
			entities
				.filter { entity ->
					entity.normalizedCode.contains(normalizedQuery) ||
						entity.normalizedName.contains(normalizedQuery)
				}
				.take(limit)
		)
	}

	override suspend fun upsertEntity(entity: SubjectCatalogCacheEntity) = Unit

	override suspend fun upsertEntities(entities: List<SubjectCatalogCacheEntity>) = Unit

	override suspend fun deleteAll(): Int = 0
}

private fun SubjectCatalogCacheEntity.toSyntheticTermSubject(): com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject {
	return com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject(
		subjectCode = subjectCode,
		name = name,
		credits = credits
	)
}

private fun academicRecord(
	approvedSubjectCodes: List<String>
): AcademicRecord {
	return AcademicRecord(
		id = "record",
		terms = listOf(
			AcademicTerm(
				id = "2025-JAN_MAR",
				periodYear = 2025,
				periodCode = AcademicTermPeriod.JAN_MAR,
				kind = TermKind.HISTORICAL,
				attempts = approvedSubjectCodes.mapIndexed { index, subjectCode ->
					AcademicAttempt(
						id = "attempt-$index",
						subjectCode = subjectCode,
						subjectName = subjectCode,
						credits = 4,
						academicOutcome = AttemptOutcome.APPROVED
					)
				}
			)
		)
	)
}

private fun academicTerm(
	id: String,
	kind: TermKind,
	periodYear: Int = 2025,
	periodCode: AcademicTermPeriod = AcademicTermPeriod.JAN_MAR,
	attempts: List<AcademicAttempt>
): AcademicTerm {
	return AcademicTerm(
		id = id,
		periodYear = periodYear,
		periodCode = periodCode,
		kind = kind,
		attempts = attempts
	)
}

private fun currentSyntheticTermPeriodOption(): com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption {
	val dateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
	return com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption(
		periodYear = dateTime.year,
		periodCode = periodForMonth(dateTime.month.ordinal + 1)
	)
}

private fun periodForMonth(month: Int): AcademicTermPeriod {
	return when (month) {
		in 1..3 -> AcademicTermPeriod.JAN_MAR
		in 4..6 -> AcademicTermPeriod.APR_JUL
		in 7..8 -> AcademicTermPeriod.JUL_AUG
		else -> AcademicTermPeriod.SEP_DEC
	}
}

private fun academicAttempt(
	subjectCode: String,
	outcome: AttemptOutcome
): AcademicAttempt {
	return AcademicAttempt(
		id = "attempt-$subjectCode",
		subjectCode = subjectCode,
		subjectName = subjectCode,
		credits = 4,
		academicOutcome = outcome
	)
}

private fun subjectCatalogEntity(
	subjectCode: String,
	name: String
): SubjectCatalogCacheEntity {
	return SubjectCatalogCacheEntity(
		subjectCode = subjectCode,
		name = name,
		credits = 4,
		gradingMode = GradingMode.NUMERIC.name,
		normalizedCode = SubjectCatalogSearchNormalizer.normalize(subjectCode),
		normalizedName = SubjectCatalogSearchNormalizer.normalize(name),
		updatedAt = 1L
	)
}

private fun pensumPayload(
	nodes: List<String>,
	edges: List<String>
): String {
	return pensumResponsePayload(
		selectedPensumId = "test-pensum",
		pensums = listOf(
			pensumPayloadPensum(
				id = "test-pensum",
				nodes = nodes,
				edges = edges
			)
		)
	)
}

private fun pensumResponsePayload(
	selectedPensumId: String,
	pensums: List<String>
): String {
	return """
		{
		  "selected_pensum_id": "$selectedPensumId",
		  "pensums": [${pensums.joinToString(separator = ",")}]
		}
	""".trimIndent()
}

private fun pensumPayloadPensum(
	id: String,
	nodes: List<String>,
	edges: List<String>
): String {
	return """
		{
		  "id": "$id",
		  "nodes": [${nodes.joinToString(separator = ",")}],
		  "edges": [${edges.joinToString(separator = ",")}]
		}
	""".trimIndent()
}

private fun pensumNode(
	id: String,
	subjectCode: String,
	name: String
): String {
	return """
		{
		  "id": "$id",
		  "node_type": "COURSE",
		  "subject_code": "$subjectCode",
		  "name": "$name",
		  "credits": 4
		}
	""".trimIndent()
}

private fun pensumEdge(
	fromNodeId: String,
	toNodeId: String,
	relationshipType: String
): String {
	return """
		{
		  "from_node_id": "$fromNodeId",
		  "to_node_id": "$toNodeId",
		  "relationship_type": "$relationshipType"
		}
	""".trimIndent()
}
