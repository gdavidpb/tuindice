package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenarios.MockJson.array
import com.gdavidpb.tuindice.scenarios.MockJson.string
import com.gdavidpb.tuindice.scenarios.SummaryHubData.long
import com.gdavidpb.tuindice.scenarios.fixture.E2eFixtures
import com.gdavidpb.tuindice.scenarios.fixture.E2eInputs
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The record and the evaluations of the two hub accounts (`E2eAccounts.SummaryHub` and `SummaryHubNoPensum`) hold what
 * the hub scenarios assert on the day of the frozen clock, and hold it in any zone a machine can run in: the scenarios
 * never name which class is in progress, because it depends on the zone of the device.
 */
class SummaryHubRecordFixturesTest {
	private val term = SummaryHubData.term
	private val now = Instant.parse(E2eFixtures.Now)

	@Test
	fun theSyncAndTheReadOfTheRecordServeTheSameRecordSoNeitherOverwritesTheOther() {
		assertEquals(SummaryHubData.syncRecord, SummaryHubData.readRecord)
	}

	@Test
	fun theOnlyCurrentTermIsTheHubTermAndItsClassesStartPutTheFrozenClockInWeekThree() {
		assertEquals(E2eFixtures.HubTerm.value, term.string("id"))

		val classesStart = LocalDate.parse(checkNotNull(term.string("classes_start_date")))
		val today = now.atOffset(ZoneOffset.UTC).toLocalDate()
		val weekOne = classesStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
		val week = ChronoUnit.DAYS.between(weekOne, today) / DAYS_PER_WEEK + 1

		assertEquals(DayOfWeek.THURSDAY, today.dayOfWeek)
		assertEquals(E2eInputs.HubWeek3.toLong(), week)
		assertEquals("SEP_DEC", term.string("period_code"))
		assertEquals("2026", term.string("period_year"))
	}

	@Test
	fun thursdayIsCoveredByBlocksOneToEightWithoutAGapSoAClassIsInProgressInEveryZoneFromMinusFourToPlusThree() {
		val thursday = SummaryHubData.meetings().filter { it.day == THURSDAY_CODE }.sortedBy { it.start }

		assertEquals(1, thursday.first().start)
		assertEquals(LAST_THURSDAY_BLOCK, thursday.last().end)
		thursday.zipWithNext().forEach { (before, after) -> assertEquals(before.end + 1, after.start) }

		for (hours in -FIRST_ZONE_HOURS..LAST_ZONE_HOURS) {
			val local = now.atOffset(ZoneOffset.ofHours(hours))
			val block = (local.hour * MINUTES_PER_HOUR + local.minute - FIRST_BLOCK_MINUTE) / BLOCK_MINUTES + 1

			assertTrue(
				thursday.any { block in it.start..it.end },
				"at 12:00Z in UTC${if (hours < 0) "" else "+"}$hours (block $block) no class is in progress"
			)
		}
	}

	@Test
	fun everyEvaluationBelongsToTheHubTermAndToOneOfItsAttemptsAndFallsAtNoonUtcSoEveryZoneReadsTheSameDay() {
		val attempts = term.array("attempts").map { (it as JsonObject).string("id") }.toSet()

		assertEquals(EVALUATION_COUNT, SummaryHubData.evaluations.size)
		SummaryHubData.evaluations.forEach { evaluation ->
			val date = checkNotNull(evaluation.long("date"))

			assertEquals(E2eFixtures.HubTerm.value, evaluation.string("term_id"))
			assertTrue(evaluation.string("attempt_id") in attempts, evaluation.string("id").orEmpty())
			assertEquals(NOON_UTC_MILLIS, date % DAY_MILLIS, evaluation.string("id"))
			// Noon UTC is the same calendar day from UTC-11 to UTC+11, which holds every zone a machine runs in.
			val days = (-DATE_ZONE_HOURS..DATE_ZONE_HOURS)
				.map { hours -> Instant.ofEpochMilli(date).atOffset(ZoneOffset.ofHours(hours)).toLocalDate() }
				.toSet()

			assertEquals(1, days.size, evaluation.string("id"))
		}
	}

	@Test
	fun theEvaluationsSitOnTheDaysTheScenariosRelyOn() {
		val graded = SummaryHubData.evaluationOf(E2eFixtures.HubEvaluationGraded.value)
		val pending = listOf(
			E2eFixtures.HubEvaluationOldest to OLDEST_DAY,
			E2eFixtures.HubEvaluationOverdue to OVERDUE_DAY,
			E2eFixtures.HubEvaluationToday to TODAY_DAY,
			E2eFixtures.HubEvaluationNoClass to NO_CLASS_DAY
		)

		val weekTwo = LocalDate.parse(checkNotNull(term.string("classes_start_date"))).plusWeeks(1)

		assertTrue(SummaryHubData.dateOf(graded) < weekTwo, "the graded one is in week 1")
		assertEquals(GRADED_GRADE, (graded["grade"] as JsonPrimitive).doubleOrNull)
		pending.forEach { (fixture, day) ->
			val evaluation = SummaryHubData.evaluationOf(fixture.value)

			assertEquals(day, SummaryHubData.dateOf(evaluation), fixture.value)
			assertTrue(evaluation["grade"] is JsonNull, "${fixture.value} has a grade")
		}
		assertEquals(DayOfWeek.THURSDAY, LocalDate.of(YEAR, OCTOBER, E2eInputs.HubClassDay).dayOfWeek)
		assertEquals(DayOfWeek.WEDNESDAY, LocalDate.of(YEAR, OCTOBER, E2eInputs.HubNoClassDay).dayOfWeek)
	}

	@Test
	fun eachEvaluationInheritsOrDoesNotInheritTheMeetingTheScenariosExpect() {
		val meetings = SummaryHubData.meetings()
		val monday = meetings.single { it.attempt == E2eFixtures.HubAttemptCi5311.value && it.day == MONDAY_CODE }
		val thursday = meetings.single { it.attempt == E2eFixtures.HubAttemptCi5437.value && it.day == THURSDAY_CODE }

		// Copy.PlaceBlocks12Mys116 and Copy.PlaceBlocks34Mys210.
		assertEquals(listOf(1, 2, "MYS-116"), listOf(monday.start, monday.end, monday.room))
		assertEquals(
			listOf(THURSDAY_FIRST_BLOCK, THURSDAY_LAST_BLOCK, "MYS-210"),
			listOf(thursday.start, thursday.end, thursday.room)
		)
		assertEquals(MONDAY_CODE, E2eInputs.ScheduleMonday)
		assertEquals(THURSDAY_CODE, E2eInputs.ScheduleThursday)
		assertEquals(E2eInputs.HubMondayStartBlock, monday.start)
		assertTrue(
			meetings.none { it.attempt == E2eFixtures.HubAttemptEg1114.value && it.day == FRIDAY_CODE },
			"EG1114 has its test on a Friday and no class that day"
		)
		assertEquals(
			listOf(MONDAY_CODE, THURSDAY_CODE),
			meetings.filter { it.attempt == E2eFixtures.HubAttemptCi5437.value }.map { it.day }.sorted(),
			"the days of the date picker's dots"
		)
	}

	@Test
	fun theUniversityReportsTheClashTheMondayOverlapProducesAndNoOtherError() {
		val attempts = term.array("attempts").map { it as JsonObject }
		val withErrors = attempts.filter { it.array("enrollment_errors").isNotEmpty() }
		val monday = SummaryHubData.meetings().filter { it.day == MONDAY_CODE }

		assertEquals(listOf(E2eFixtures.HubAttemptCi5437.value), withErrors.map { it.string("id") })
		assertEquals(
			listOf("CHOQUE DE HORARIO"),
			withErrors.single().array("enrollment_errors").map { (it as JsonPrimitive).content }
		)
		assertTrue(monday.size == 2 && monday[0].start <= monday[1].end && monday[1].start <= monday[0].end)
	}

	private companion object {
		const val DAYS_PER_WEEK = 7
		const val EVALUATION_COUNT = 5
		const val MONDAY_CODE = 2
		const val THURSDAY_CODE = 5
		const val FRIDAY_CODE = 6
		const val LAST_THURSDAY_BLOCK = 8
		const val THURSDAY_FIRST_BLOCK = 3
		const val THURSDAY_LAST_BLOCK = 4
		const val FIRST_ZONE_HOURS = 4
		const val LAST_ZONE_HOURS = 3
		const val DATE_ZONE_HOURS = 11
		const val MINUTES_PER_HOUR = 60
		const val FIRST_BLOCK_MINUTE = 7 * 60 + 30
		const val BLOCK_MINUTES = 60
		const val DAY_MILLIS = 24 * 60 * 60 * 1000L
		const val NOON_UTC_MILLIS = 12 * 60 * 60 * 1000L
		const val GRADED_GRADE = 8.0
		const val YEAR = 2026
		const val OCTOBER = 10
		val OLDEST_DAY: LocalDate = LocalDate.of(YEAR, OCTOBER, 6)
		val OVERDUE_DAY: LocalDate = LocalDate.of(YEAR, OCTOBER, 12)
		val TODAY_DAY: LocalDate = LocalDate.of(YEAR, OCTOBER, 15)
		val NO_CLASS_DAY: LocalDate = LocalDate.of(YEAR, OCTOBER, 16)
	}
}
