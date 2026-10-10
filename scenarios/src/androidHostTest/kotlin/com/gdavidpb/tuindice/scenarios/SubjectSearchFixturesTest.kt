package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenarios.MockJson.array
import com.gdavidpb.tuindice.scenarios.MockJson.string
import com.gdavidpb.tuindice.scenarios.fixture.E2eFixtures
import com.gdavidpb.tuindice.scenarios.fixture.E2eInputs
import com.gdavidpb.tuindice.scenarios.fixture.SearchExpectation
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The subject searches of the subjects scenarios list what they wait for. The catalog mock serves `ci`; the debug
 * source of the subjects module lists `qa`, `qb` and `rx` and answers nothing under the minimum length, which the
 * test reads from the product. A fabricated search the sources do not support must be noticed.
 */
class SubjectSearchFixturesTest {
	private val debugSource = RepoFiles.file(
		"subjects/src/commonMain/kotlin/com/gdavidpb/tuindice/subjects/data/source/DebugSubjectScenarioResolver.kt"
	)
	private val machineSource = RepoFiles.file(
		"subjects/src/commonMain/kotlin/com/gdavidpb/tuindice/subjects/presentation/machine/SubjectSearchMachine.kt"
	)

	/** `subject("QA", "REMOTE UNAVAILABLE SUBJECT", 3)` of the debug source, by code. */
	private val debugSubjects: Map<String, String> = Regex("""subject\("(\w+)", "([^"]*)"""")
		.findAll(debugSource.readText()).associate { it.groupValues[1] to it.groupValues[2] }

	private val minimumLength: Int =
		Regex("""MinimumSubjectSearchQueryLength = (\d+)""").find(machineSource.readText())!!.groupValues[1].toInt()

	@Test
	fun theSourcesWereReadAndTheMinimumIsPlausible() {
		assertTrue(debugSubjects.size > MINIMUM_DEBUG_SUBJECTS, "only ${debugSubjects.size} debug subjects parsed")
		assertTrue(minimumLength >= 2, "the minimum query length is $minimumLength")
	}

	@Test
	fun everySearchedSubjectIsListedForItsQueryByTheCatalogMockOrTheDebugSource() {
		E2eFixtures.subjectSearches.forEach { search ->
			val unlisted = unlisted(search)

			assertTrue(unlisted.isEmpty(), "query '${search.query}' does not list $unlisted")
		}
	}

	@Test
	fun aSearchTheSourcesDoNotSupportIsCaught() {
		assertEquals(listOf("CI2511"), unlisted(SearchExpectation("qa", listOf("CI2511", "QA"))))
		assertEquals(listOf("ZZ9999"), unlisted(SearchExpectation("ci", listOf("CI2511", "ZZ9999"))))
	}

	@Test
	fun everySubjectSearchQueryIsLongEnoughToSearchAndTheShortOneIsNot() {
		assertTrue(E2eFixtures.subjectSearches.all { it.query.length >= minimumLength })
		assertTrue(E2eInputs.SubjectQueryTooShort.length < minimumLength)
	}

	@Test
	fun theNoMatchQueryMatchesNoSubjectOfTheCatalogMockNorOfTheDebugSource() {
		val query = E2eInputs.SubjectQueryNoMatch
		val matching = knownSubjects("ci").filter { (code, name) ->
			code.contains(query, ignoreCase = true) || name.contains(query, ignoreCase = true)
		}

		assertTrue(knownSubjects("ci").size > MINIMUM_DEBUG_SUBJECTS)
		assertTrue(query.length >= minimumLength)
		assertEquals(emptyMap(), matching)
	}

	/** Codes of [search] that neither list under its query nor carry the query in their code or name. */
	private fun unlisted(search: SearchExpectation): List<String> {
		val known = knownSubjects(search.query)

		return search.subjectCodes.filter { code ->
			val name = known[code]

			name == null || !(code.contains(search.query, ignoreCase = true) || name.contains(search.query, ignoreCase = true))
		}
	}

	/** Code to name of what the catalog mock serves for [query], over what the debug source lists. */
	private fun knownSubjects(query: String): Map<String, String> {
		val served = SearchMocks.bodyFor(query).root.array("results").filterIsInstance<JsonObject>()
			.mapNotNull { result -> result.string("subject_code")?.let { it to result.string("name").orEmpty() } }

		return debugSubjects + served
	}

	private companion object {
		const val MINIMUM_DEBUG_SUBJECTS = 20
	}
}
