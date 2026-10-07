package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenarios.MockJson.array
import com.gdavidpb.tuindice.scenarios.MockJson.string
import com.gdavidpb.tuindice.scenarios.fixture.E2eFixtures
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The record search must answer as the scenarios type it, and the canonical record must hold what they read. */
class RecordSearchFixturesTest {
	private val syncSuccess = "mocks/__files/sync/post-sync-success.json"
	private val pensum2016 = "mocks/__files/pensums/get-pensum-2016-degree_project.json"

	@Test
	fun everySearchedSubjectIsInTheResultsTheMocksServeForTheQuery() {
		E2eFixtures.recordSearches.forEach { search ->
			val body = resolveSearchBody(search.query)
			val codes = body.root.array("results").mapNotNull { (it as? JsonObject)?.string("subject_code") }

			search.subjectCodes.forEach { code ->
				assertTrue(code in codes, "query '${search.query}' resolves to ${body.fileName} without $code")
			}
		}
	}

	@Test
	fun theSearchedSubjectsAreFixtures() {
		val known = E2eFixtures.all.map { it.value }.toSet()

		E2eFixtures.recordSearches.flatMap { it.subjectCodes }.forEach { assertTrue(it in known, it) }
	}

	@Test
	fun theHistoricalOutcomesOfTheCanonicalRecord() {
		val record = MockJson.obj(RepoFiles.file(syncSuccess))

		assertEquals("approved", outcomeOf(record, E2eFixtures.SubjectMa1111.value))
		assertEquals("retired", outcomeOf(record, E2eFixtures.SubjectMa1112.value))
		assertEquals("failed", outcomeOf(record, E2eFixtures.SubjectMa1121.value))
	}

	@Test
	fun theTooltipTermContextOfTheCanonicalRecord() {
		val record = MockJson.obj(RepoFiles.file(syncSuccess))

		assertEquals(
			listOf("synthetic", "pending", "2026", "JUL_AUG"),
			termContext(record, E2eFixtures.SubjectEp1308.value)
		)
		assertEquals(
			listOf("historical", "approved", "2021", "SEP_DEC"),
			termContext(record, E2eFixtures.SubjectMa1111.value)
		)
	}

	@Test
	fun theBlockedSubjectMissesItsTwoRequirementsInThePensum() {
		val pensum = MockJson.obj(RepoFiles.file(pensum2016))
		val selected = pensum.string("selected_pensum_id")
		val chosen = pensum.array("pensums").filterIsInstance<JsonObject>().firstOrNull { it.string("id") == selected }
			?: checkNotNull(pensum["pensum"] as? JsonObject)
		val nodes = chosen.array("nodes").filterIsInstance<JsonObject>()
		val blockedNode = nodes.first { it.string("subject_code") == E2eFixtures.SubjectEp2308.value }.string("id")
		val requirements = chosen.array("edges").filterIsInstance<JsonObject>()
			.filter { it.string("to_node_id") == blockedNode && it.string("relationship_type") == "REQUIREMENT" }
			.mapNotNull { edge ->
				nodes.firstOrNull { it.string("id") == edge.string("from_node_id") }?.string("subject_code")
			}
			.sorted()

		assertEquals(
			listOf(E2eFixtures.SubjectEp1308.value, E2eFixtures.SubjectEp5855.value),
			requirements
		)
	}

	private class ResolvedBody(val fileName: String, val root: JsonObject)

	/** The search mock WireMock picks for [query]: lowest priority number among the matching GET stubs. */
	private fun resolveSearchBody(query: String): ResolvedBody {
		val candidates = RepoFiles.allMappings.resolve("subjects")
			.listFiles { file -> file.name.startsWith("search-subjects-") }
			.orEmpty()
			.map { MockJson.obj(it) }
			.filter { mapping ->
				val request = mapping["request"] as JsonObject
				val contains = ((request["queryParameters"] as? JsonObject)?.get("query") as? JsonObject)
					?.string("contains")

				request.string("method") == "GET" &&
					request.string("urlPath") == "/subjects/v1/search" &&
					(mapping["response"] as JsonObject).string("bodyFileName") != null &&
					(contains == null || contains in query)
			}
		val best = candidates.minBy { (it["priority"] as? JsonPrimitive)?.intOrNull ?: DEFAULT_PRIORITY }
		val bodyName = checkNotNull((best["response"] as JsonObject).string("bodyFileName"))

		return ResolvedBody(bodyName, MockJson.obj(RepoFiles.file("mocks/__files/$bodyName")))
	}

	private fun outcomeOf(record: JsonObject, code: String): String? =
		termsOf(record)
			.flatMap { it.array("attempts").filterIsInstance<JsonObject>() }
			.firstOrNull { it.string("subject_code") == code }
			?.string("academic_outcome")

	private fun termContext(record: JsonObject, code: String): List<String?> {
		val term = termsOf(record).first { term ->
			term.array("attempts").any { (it as? JsonObject)?.string("subject_code") == code }
		}
		val outcome = term.array("attempts").filterIsInstance<JsonObject>()
			.first { it.string("subject_code") == code }.string("academic_outcome")

		return listOf(
			term.string("term_kind"),
			outcome,
			(term["period_year"] as? JsonPrimitive)?.content,
			term.string("period_code")
		)
	}

	private fun termsOf(record: JsonObject): List<JsonObject> =
		(((record["record"] as JsonObject)["record"]) as JsonObject).array("terms").filterIsInstance<JsonObject>()

	private companion object {
		const val DEFAULT_PRIORITY = 5
	}
}
