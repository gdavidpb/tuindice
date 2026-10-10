package com.gdavidpb.tuindice.record.testing

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicScheduleEntry
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptBadge
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.record.domain.model.ScheduleNow
import com.gdavidpb.tuindice.record.presentation.mapper.toScheduleItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.presentation.model.ScheduleItem
import kotlin.math.abs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

// Monday at 9:00: halfway through block 2 of the assumed block clock, which starts block 1 at 7:30.
val MondayAtNine = ScheduleNow(dayOfWeek = 2, minuteOfDay = 9 * 60)

// Laid out by the mapper, so what a schedule view is handed in a test is what the app resolves:
// the cells, the lanes of a clash, the class in progress and every text shown or said.
fun scheduleItem(vararg attempts: AttemptProjection, now: ScheduleNow? = null): ScheduleItem =
	assertNotNull(attempts.toList().toScheduleItem(now = now))

/**
 * The week most schedule tests draw: CI5311 meets Monday (blocks 1-2) and Wednesday (block 3) in
 * MYS-116, CI5437 meets Monday (blocks 2-3) with no room and so clashes with it on block 2, and
 * EG1114 has nothing placed yet.
 */
fun clashingWeek(now: ScheduleNow? = null): ScheduleItem = scheduleItem(
	scheduleAttempt(
		id = "a1",
		code = "CI5311",
		section = 1,
		schedule = listOf(
			scheduleEntry(day = ScheduleDay.Monday, blocks = 1..2, classroom = "MYS-116"),
			scheduleEntry(day = ScheduleDay.Wednesday, blocks = 3..3, classroom = "MYS-116")
		)
	),
	scheduleAttempt(
		id = "a2",
		code = "CI5437",
		section = 2,
		schedule = listOf(scheduleEntry(day = ScheduleDay.Monday, blocks = 2..3))
	),
	scheduleAttempt(id = "a3", code = "EG1114"),
	now = now
)

fun scheduleEntry(day: ScheduleDay, blocks: IntRange, classroom: String = "") = AcademicScheduleEntry(
	dayOfWeek = day.code,
	startBlock = blocks.first,
	endBlock = blocks.last,
	classroom = classroom
)

fun scheduleAttempt(
	id: String,
	code: String,
	section: Int? = null,
	schedule: List<AcademicScheduleEntry>? = null
) = AttemptProjection(
	id = id,
	subjectCode = code,
	subjectName = code,
	credits = 3,
	gradingMode = AttemptGradingMode.NUMERIC,
	score = AttemptScore.empty(),
	outcome = AttemptOutcome.PENDING,
	badge = AttemptBadge.NONE,
	section = section,
	schedule = schedule
)

// How a text node was laid out: its resolved style (the weight a name is drawn in) and its lines.
fun SemanticsNodeInteraction.textLayout(): TextLayoutResult {
	val results = mutableListOf<TextLayoutResult>()

	fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(results)

	return results.single()
}

// Whether a text had room for all of itself on its one line, or was cut to fit the space it got.
val TextLayoutResult.fitsWhole: Boolean
	get() = lineCount == 1 && size.width >= multiParagraph.maxIntrinsicWidth

// Positions are compared between two nodes of the same test, so where the host sits in the window
// does not matter. Half a dp absorbs the rounding of a fractional offset to whole pixels.
fun assertDpEquals(expected: Dp, actual: Dp, what: String) {
	assertTrue(
		actual = abs((expected - actual).value) <= DpTolerance.value,
		message = "$what: expected $expected but was $actual"
	)
}

private val DpTolerance = 0.5.dp
