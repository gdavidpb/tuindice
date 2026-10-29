package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleClash
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduleClashEngineTest {
	private companion object {
		const val MONDAY = 2
		const val TUESDAY = 3
	}

	private fun meeting(
		code: String,
		start: Int,
		end: Int = start,
		day: Int = MONDAY,
		section: Int? = 1
	) = ScheduleMeeting(
		attemptId = "attempt-$code",
		subjectCode = code,
		section = section,
		dayOfWeek = day,
		startBlock = start,
		endBlock = end,
		classroom = "R1"
	)

	private fun clash(first: ScheduleMeeting, second: ScheduleMeeting) = ScheduleClash(
		day = first.dayOfWeek,
		first = first,
		second = second
	)

	@Test
	fun withoutSharedBlocks_thereIsNoClash() {
		val meetings = listOf(
			meeting("MA1111", start = 1, end = 2),
			meeting("CI2525", start = 1, end = 2, day = TUESDAY)
		)

		assertTrue(ScheduleClashEngine.clashesOf(meetings).isEmpty())
	}

	@Test
	fun twoSubjectsThatShareABlock_clash_andTheOneThatStartsFirstIsFirst() {
		val ma = meeting("MA1111", start = 1, end = 2)
		val ci = meeting("CI2525", start = 2, end = 3)

		assertEquals(listOf(clash(ma, ci)), ScheduleClashEngine.clashesOf(listOf(ci, ma)))
	}

	@Test
	fun meetingsThatOnlyTouch_doNotClash_likeTheGrid() {
		val meetings = listOf(
			meeting("MA1111", start = 1, end = 2),
			meeting("CI2525", start = 3, end = 4)
		)

		assertTrue(ScheduleClashEngine.clashesOf(meetings).isEmpty())
	}

	@Test
	fun threeMeetingsWithTwoClashes_areTwoPairs_inBlockOrder() {
		val a = meeting("MA1111", start = 1, end = 2)
		val b = meeting("CI2525", start = 2, end = 3)
		val c = meeting("EG1114", start = 3, end = 4)

		assertEquals(listOf(clash(a, b), clash(b, c)), ScheduleClashEngine.clashesOf(listOf(c, a, b)))
	}

	@Test
	fun theSameSubjectAndSection_inTwoMeetings_doesNotClash() {
		val meetings = listOf(
			meeting("MA1111", start = 1, end = 2),
			meeting("MA1111", start = 2, end = 3)
		)

		assertTrue(ScheduleClashEngine.clashesOf(meetings).isEmpty())
	}

	@Test
	fun theSameSubjectInAnotherSection_clashes() {
		val first = meeting("MA1111", start = 1, end = 2, section = 1)
		val other = meeting("MA1111", start = 2, end = 3, section = 2)

		assertEquals(listOf(clash(first, other)), ScheduleClashEngine.clashesOf(listOf(other, first)))
	}

	@Test
	fun aPairIsReportedOnce_evenIfAMeetingIsListedTwice() {
		val ma = meeting("MA1111", start = 1, end = 2)
		val ci = meeting("CI2525", start = 2, end = 3)

		assertEquals(listOf(clash(ma, ci)), ScheduleClashEngine.clashesOf(listOf(ma, ci, ci)))
	}

	@Test
	fun theOrderIsDeterministic_byDay_thenBlock_thenCode() {
		val ma = meeting("MA1111", start = 1, end = 2)
		val zz = meeting("ZZ9999", start = 2)
		val ci = meeting("CI2525", start = 5)
		val de = meeting("DE1000", start = 5)
		val eg = meeting("EG1114", start = 5)
		val aa = meeting("AA1111", start = 1, day = TUESDAY)
		val bb = meeting("BB2222", start = 1, day = TUESDAY)
		val expected = listOf(
			clash(ma, zz),
			clash(ci, de),
			clash(ci, eg),
			clash(de, eg),
			clash(aa, bb)
		)
		val shuffled = listOf(bb, eg, zz, aa, de, ma, ci)

		assertEquals(expected, ScheduleClashEngine.clashesOf(shuffled))
		assertEquals(expected, ScheduleClashEngine.clashesOf(shuffled.reversed()))
	}
}
