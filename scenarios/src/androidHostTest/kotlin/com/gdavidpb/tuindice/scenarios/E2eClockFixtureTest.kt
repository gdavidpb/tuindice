package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenarios.MockJson.array
import com.gdavidpb.tuindice.scenarios.MockJson.string
import com.gdavidpb.tuindice.scenarios.fixture.Copy
import com.gdavidpb.tuindice.scenarios.fixture.E2eFixtures
import com.gdavidpb.tuindice.scenarios.fixture.E2eInputs
import kotlinx.serialization.json.JsonObject
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The texts and keys the scenarios derive from a date (`Copy.TermSepDec2026`, `E2eFixtures.NextTermKey`) are
 * the ones the app builds from the instant the scenarios freeze it at (`E2eFixtures.Now`): the first term it
 * offers for planning on top of the canonical record. The app reads the term in the university's time zone.
 */
class E2eClockFixtureTest {
	private val universityZone = ZoneId.of("America/Caracas")
	private val now = Instant.parse(E2eFixtures.Now).atZone(universityZone)
	private val recordTerms = MockJson.obj(RepoFiles.file(CANONICAL_SYNC))
		.let { sync -> sync["record"] as JsonObject }
		.let { revision -> (revision["record"] as JsonObject).array("terms") }
		.map { it as JsonObject }

	@Test
	fun theFrozenInstantFallsInTheSeptemberDecemberTermOf2026() {
		assertEquals(YEAR, now.year)
		assertTrue(
			now.monthValue in SEPTEMBER..DECEMBER,
			"'${E2eFixtures.Now}' is month ${now.monthValue} in ${universityZone.id}"
		)
	}

	@Test
	fun theDerivedKeyAndTextNameTheTermTheFrozenInstantFallsIn() {
		assertEquals("${now.year}-SEP_DEC", E2eFixtures.NextTermKey.value)
		assertEquals("Sep - Dic ${now.year}", Copy.TermSepDec2026)
		// The second option the period selector lists: the first one of the next year.
		assertEquals("Ene - Mar ${now.year + 1}", Copy.TermJanMar2027)
	}

	@Test
	fun thatTermIsFreeInTheCanonicalRecordSoTheAppOffersIt() {
		val taken = recordTerms.map { term -> term.string("period_year") + "-" + term.string("period_code") }

		assertTrue(taken.isNotEmpty())
		assertFalse(E2eFixtures.NextTermKey.value in taken, "the record already has ${E2eFixtures.NextTermKey.value}")
	}

	@Test
	fun noTermOfTheCanonicalRecordIsLaterThanTheFrozenTerm() {
		val years = recordTerms.map { term -> term.string("period_year")!!.toInt() }

		assertTrue(years.max() <= now.year, "a term of the record is after ${now.year}")
	}

	@Test
	fun theFrozenInstantIsPastTheTwelfthWeekOfTheCurrentTermSoTheEvaluationsStripOpensOnTheLast() {
		val current = recordTerms.single { it.string("term_kind") == "current" }
		val startMonth = checkNotNull(START_MONTH_OF[current.string("period_code")]) { "unknown period of the current term" }
		val firstMonday = LocalDate.of(current.string("period_year")!!.toInt(), startMonth, 1)
			.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
		val week = ChronoUnit.DAYS.between(firstMonday, now.toLocalDate()) / DAYS_PER_WEEK + 1
		val lastWeek = Regex("MAX_ACADEMIC_WEEK = (\\d+)").find(RepoFiles.file(ACADEMIC_WEEK).readText())!!
			.groupValues[1].toInt()

		assertEquals(lastWeek, E2eInputs.LastAcademicWeek, "the last week of the strip")
		assertTrue(week >= lastWeek, "the frozen instant is in week $week of the current term, short of the last ($lastWeek)")
	}

	private companion object {
		const val ACADEMIC_WEEK =
			"evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/presentation/mapper/AcademicWeek.kt"
		const val DAYS_PER_WEEK = 7
		val START_MONTH_OF = mapOf("JAN_MAR" to 1, "APR_JUL" to 4, "JUL_AUG" to 7, "SEP_DEC" to 9)
		const val CANONICAL_SYNC = "mocks/__files/sync/post-sync-success.json"
		const val YEAR = 2026
		const val SEPTEMBER = 9
		const val DECEMBER = 12
	}
}
