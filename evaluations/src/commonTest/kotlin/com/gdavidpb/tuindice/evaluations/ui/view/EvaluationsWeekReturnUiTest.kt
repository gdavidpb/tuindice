package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class EvaluationsWeekReturnUiTest {
	// The student drags the list into week 6 and back to week 5 while both choices are held on their way. The host keeps
	// only the last one, week 5, which it already shows, so no change of the selection reaches the list. The student's
	// next tap on week 6 must still move the list.
	@Test
	fun when_theListGoesToAnotherWeekAndBackWhileTheChoicesAreHeld_then_theNextChoiceOfThatWeekMovesTheList() =
		runTuIndiceUiTest {
			val week5 = EvaluationsWeekKey.Academic(5)
			val week6 = EvaluationsWeekKey.Academic(6)
			val chosenWeek = mutableStateOf<EvaluationsWeekKey>(week5)
			val drag = mutableStateOf<DragRequest?>(null)
			val host = setWeekListHost(
				weeks = evaluationWeeksFixture(count = 12, cardsPerWeek = 4),
				chosenWeek = chosenWeek,
				drag = drag
			)
			host.isHoldingChoices = true
			waitForIdle()

			dragUntilReported(host = host, drag = drag, count = 1, stepPx = REPORT_STEP_PX)
			dragUntilReported(host = host, drag = drag, count = 2, stepPx = -REPORT_STEP_PX)
			assertEquals<List<EvaluationsWeekKey>>(listOf(week6, week5), host.reportedWeeks)

			// The held choices reach the host, which keeps the last one: week 5, the week it already shows.
			runOnIdle {
				host.isHoldingChoices = false
				host.heldChoice?.let { heldWeek -> chosenWeek.value = heldWeek }
			}
			waitForIdle()
			assertEquals(week5, chosenWeek.value)

			// The student taps week 6 on the strip: the list has to move to it.
			runOnIdle { chosenWeek.value = week6 }
			waitForIdle()

			assertEquals(week6, chosenWeek.value)
			assertHeaderAtTopOfList(6)
		}

	// The student taps week 5 and then scrolls the list within it. Tapping week 5 again, the week the list already
	// stands on, must leave the list where the student scrolled it: a tap on the placed week is no move, not even back
	// to its top.
	@Test
	fun when_theStudentTapsThePlacedWeekAgain_then_theListStaysWhereTheStudentScrolledIt() =
		runTuIndiceUiTest {
			val week3 = EvaluationsWeekKey.Academic(3)
			val week5 = EvaluationsWeekKey.Academic(5)
			val chosenWeek = mutableStateOf<EvaluationsWeekKey>(week3)
			val drag = mutableStateOf<DragRequest?>(null)
			val host = setWeekListHost(
				weeks = evaluationWeeksFixture(count = 12, cardsPerWeek = 4),
				chosenWeek = chosenWeek,
				drag = drag
			)
			waitForIdle()

			// The student taps week 5: the list goes to its header.
			runOnIdle { chosenWeek.value = week5 }
			waitForIdle()
			assertEquals(WEEK_5_HEADER_INDEX, host.listState.firstVisibleItemIndex)

			// The student scrolls the list within week 5, which is no change of week, so nothing is reported.
			runOnIdle { drag.value = DragRequest(steps = 1, stepPx = WITHIN_WEEK_STEP_PX) }
			waitForIdle()
			assertEquals<List<EvaluationsWeekKey>>(emptyList(), host.reportedWeeks)
			assertTrue(host.listState.firstVisibleItemIndex in WEEK_5_HEADER_INDEX until WEEK_6_HEADER_INDEX)

			val scrolledTo = host.listState.position()
			assertTrue(scrolledTo != (WEEK_5_HEADER_INDEX to 0), "the list was scrolled within week 5 at $scrolledTo")

			// The student taps week 5 again, the week the list already stands on: the list stays where it was scrolled.
			runOnIdle { chosenWeek.value = week5 }
			waitForIdle()

			assertEquals(scrolledTo, host.listState.position())
		}

	// Drags the list a step at a time, each step a gesture of its own, until the host has been reported [count] weeks.
	private fun ComposeUiTest.dragUntilReported(
		host: WeekListHost,
		drag: MutableState<DragRequest?>,
		count: Int,
		stepPx: Float
	) {
		var attempts = 0

		while (host.reportedWeeks.size < count && attempts < MAX_DRAG_ATTEMPTS) {
			attempts += 1
			runOnIdle { drag.value = DragRequest(steps = 1, stepPx = stepPx) }
			waitForIdle()
		}
	}

	private companion object {
		const val REPORT_STEP_PX = 100f
		const val MAX_DRAG_ATTEMPTS = 80
		const val WEEK_5_HEADER_INDEX = 20
		const val WEEK_6_HEADER_INDEX = 25
		const val WITHIN_WEEK_STEP_PX = 60f
	}
}
