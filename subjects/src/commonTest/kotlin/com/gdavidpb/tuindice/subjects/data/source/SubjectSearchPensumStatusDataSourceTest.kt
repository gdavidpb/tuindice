package com.gdavidpb.tuindice.subjects.data.source

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicPensumStatusEngine
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.persistence.data.room.daos.PensumCacheDao
import com.gdavidpb.tuindice.persistence.data.room.daos.PensumSelectionDao
import com.gdavidpb.tuindice.persistence.data.room.entity.PensumCacheEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.PensumSelectionEntity
import com.gdavidpb.tuindice.persistence.domain.repository.VisibleAcademicRecordRepository
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSearchPensumStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class SubjectSearchPensumStatusDataSourceTest {
	@Test
	fun resolver_labelsCoursesTheirEquivalencesFulfilledCodesAndOpenSlots() = runTest {
		val resolver = dataSource(
			payloadJson = PENSUM_PAYLOAD,
			approvedSubjectCodes = listOf("MA1111", "MC5122")
		).observePensumStatusResolver().first()

		data class Case(val description: String, val code: String, val credits: Int, val status: SubjectSearchPensumStatus?)

		val cases = listOf(
			Case("approved fixed course", "MA1111", 4, SubjectSearchPensumStatus.APPROVED),
			Case("equivalence code of an available course", "ci2521", 4, SubjectSearchPensumStatus.AVAILABLE),
			Case("elective that already filled the slot", "MC5122", 3, SubjectSearchPensumStatus.APPROVED),
			// The only elective slot is taken: counting toward nothing earns no badge.
			Case("elective whose slots are filled", "MC5123", 3, null),
			Case("open general studies slot", "CS2316", 3, SubjectSearchPensumStatus.COUNTS_AS_GENERAL_STUDIES),
			Case("below the slot's minimum credits", "CS2316", 2, null),
			Case("unknown to the pensum", "ZZ9999", 3, null)
		)

		cases.forEach { case ->
			assertEquals(case.status, resolver.statusOf(subjectCode = case.code, credits = case.credits), case.description)
		}
	}

	@Test
	fun resolver_labelsAnOpenElectiveSlot() = runTest {
		val resolver = dataSource(payloadJson = PENSUM_PAYLOAD, approvedSubjectCodes = emptyList())
			.observePensumStatusResolver()
			.first()

		assertEquals(SubjectSearchPensumStatus.COUNTS_AS_ELECTIVE, resolver.statusOf(subjectCode = "MC5123", credits = 3))
	}

	@Test
	fun resolver_withoutACachedPensum_labelsNothing() = runTest {
		val resolver = dataSource(payloadJson = null, approvedSubjectCodes = listOf("MA1111"))
			.observePensumStatusResolver()
			.first()

		assertEquals(null, resolver.statusOf(subjectCode = "MA1111", credits = 4))
		assertEquals(null, resolver.statusOf(subjectCode = "MC5123", credits = 3))
	}

	private fun dataSource(payloadJson: String?, approvedSubjectCodes: List<String>): SubjectSearchPensumStatusDataSource {
		return SubjectSearchPensumStatusDataSource(
			pensumSelectionDao = SinglePensumSelectionDao(
				PensumSelectionEntity(year = 2019, modalityId = "degree_project", cacheKey = CACHE_KEY, updatedAt = 0L)
			),
			pensumCacheDao = SinglePensumCacheDao(
				payloadJson?.let { payload ->
					PensumCacheEntity(
						cacheKey = CACHE_KEY,
						year = 2019,
						modalityId = "degree_project",
						payloadJson = payload,
						updatedAt = 0L
					)
				}
			),
			visibleAcademicRecordRepository = StaticVisibleAcademicRecordRepository(record(approvedSubjectCodes)),
			json = Json { ignoreUnknownKeys = true },
			statusEngine = AcademicPensumStatusEngine()
		)
	}

	private fun record(approvedSubjectCodes: List<String>): AcademicRecord {
		return AcademicRecord(
			id = "record",
			terms = listOf(
				AcademicTerm(
					id = "2025-JAN_MAR",
					periodYear = 2025,
					periodCode = AcademicTermPeriod.JAN_MAR,
					kind = TermKind.HISTORICAL,
					attempts = approvedSubjectCodes.mapIndexed { index, code ->
						AcademicAttempt(
							id = "attempt-$index",
							subjectCode = code,
							subjectName = code,
							credits = if (code.startsWith("MA")) 4 else 3,
							academicOutcome = AttemptOutcome.APPROVED
						)
					}
				)
			)
		)
	}
}

private const val CACHE_KEY = "2019-degree_project"

private val PENSUM_PAYLOAD = """
	{
	  "selected_pensum_id": "test-pensum",
	  "pensums": [{
	    "id": "test-pensum",
	    "nodes": [
	      {"id": "ma1111", "node_type": "COURSE", "subject_code": "MA1111", "credits": 4},
	      {"id": "ci2525", "node_type": "COURSE", "subject_code": "CI2525", "credits": 4,
	       "fulfillment_rules": [{"rule_type": "EQUIVALENCE", "subject_codes": ["CI2521"]}]},
	      {"id": "elective-1", "node_type": "SLOT", "credits": 3, "category": "AREA_ELECTIVE",
	       "fulfillment_rules": [{"rule_type": "SUBJECT_ELIGIBILITY", "slot_eligibility_kind": "ELECTIVE",
	         "subject_codes": ["MC5122", "MC5123"], "min_credits": 3, "min_subjects": 1}]},
	      {"id": "eg-1", "node_type": "SLOT", "credits": 3, "category": "GENERAL_STUDIES",
	       "fulfillment_rules": [{"rule_type": "SUBJECT_ELIGIBILITY", "slot_eligibility_kind": "GENERAL",
	         "subject_codes": ["CS2316"], "min_credits": 3, "min_subjects": 1}]}
	    ],
	    "edges": []
	  }]
	}
""".trimIndent()

private class SinglePensumSelectionDao(
	private val selection: PensumSelectionEntity?
) : PensumSelectionDao() {
	override fun observeSelection(id: String): Flow<PensumSelectionEntity?> = flowOf(selection)

	override suspend fun getSelection(id: String): PensumSelectionEntity? = selection

	override suspend fun upsertEntity(entity: PensumSelectionEntity) = Unit

	override suspend fun upsertEntities(entities: List<PensumSelectionEntity>) = Unit

	override suspend fun deleteAll(): Int = 0
}

private class SinglePensumCacheDao(private val cache: PensumCacheEntity?) : PensumCacheDao() {
	override fun observePensum(cacheKey: String): Flow<PensumCacheEntity?> {
		return flowOf(cache?.takeIf { entity -> entity.cacheKey == cacheKey })
	}

	override suspend fun getPensum(cacheKey: String): PensumCacheEntity? {
		return cache?.takeIf { entity -> entity.cacheKey == cacheKey }
	}

	override suspend fun getPensum(year: Int, modalityId: String): PensumCacheEntity? = null

	override suspend fun upsertEntity(entity: PensumCacheEntity) = Unit

	override suspend fun upsertEntities(entities: List<PensumCacheEntity>) = Unit

	override suspend fun deleteAll(): Int = 0
}

private class StaticVisibleAcademicRecordRepository(
	private val record: AcademicRecord
) : VisibleAcademicRecordRepository {
	override fun observeVisibleAcademicRecordFlow(): Flow<AcademicRecord?> = flowOf(record)
}
