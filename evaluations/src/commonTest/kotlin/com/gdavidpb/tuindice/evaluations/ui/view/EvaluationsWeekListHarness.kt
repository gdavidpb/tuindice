@file:OptIn(ExperimentalTestApi::class)

package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.testing.evaluationItemFixture
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.evaluations.ui.model.EvaluationsActions
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.test.assertTrue

// A drag of the list by the student: a scroll with the user's priority, which cuts any move the list is making.
internal suspend fun LazyListState.dragBy(steps: Int, stepPx: Float) {
	scroll(MutatePriority.UserInput) {
		repeat(steps) {
			scrollBy(stepPx)
			withFrameNanos { }
		}
	}
}

// Where the list stands: the first item in view and how far it is scrolled.
internal fun LazyListState.position(): Pair<Int, Int> = firstVisibleItemIndex to firstVisibleItemScrollOffset

internal fun ComposeUiTest.advanceFrames(count: Int) {
	repeat(count) { mainClock.advanceTimeByFrame() }
}

internal fun ComposeUiTest.assertHeaderAtTopOfList(weekNumber: Int) {
	val list = onNodeWithTag(EvaluationsUiTags.EvaluationsList).getUnclippedBoundsInRoot()
	val header = onNodeWithTag(EvaluationsUiTags.evaluationsWeekHeader(weekNumber)).getUnclippedBoundsInRoot()

	assertTrue(
		abs((header.top - list.top).value) < 1f,
		"the header of week $weekNumber at ${header.top} is at the top of the list at ${list.top}"
	)
}

// Sets the list under a host that keeps the chosen week, and returns what the list reported and took. A choice made
// by the list is applied after [applyDelayFrames] frames, each one on its own, so several can be on their way at once;
// a drag requested through [drag] is made by the list.
internal fun ComposeUiTest.setWeekListHost(
	weeks: List<EvaluationsWeekGroupItem>,
	chosenWeek: MutableState<EvaluationsWeekKey>,
	drag: MutableState<DragRequest?>,
	arrivalEvaluationId: MutableState<String?> = mutableStateOf(null),
	applyDelayFrames: Int = 0
): WeekListHost = setWeekListHost(mutableStateOf(weeks), chosenWeek, drag, arrivalEvaluationId, applyDelayFrames)

// The same, with the weeks the list shows held in a state, so that they can change under it.
internal fun ComposeUiTest.setWeekListHost(
	weeks: State<List<EvaluationsWeekGroupItem>>,
	chosenWeek: MutableState<EvaluationsWeekKey>,
	drag: MutableState<DragRequest?>,
	arrivalEvaluationId: MutableState<String?> = mutableStateOf(null),
	applyDelayFrames: Int = 0
): WeekListHost {
	val host = WeekListHost(listState = LazyListState())

	setTuIndiceTestContent {
		val scope = rememberCoroutineScope()

		LaunchedEffect(drag.value) {
			drag.value?.let { request -> host.listState.dragBy(steps = request.steps, stepPx = request.stepPx) }
		}

		EvaluationsView(
			lazyListState = host.listState,
			weekGroups = weeks.value,
			selectedWeekKey = chosenWeek.value,
			actions = EvaluationsActions(
				onWeekClick = { weekKey ->
					host.reportedWeeks += weekKey

					if (host.isHoldingChoices) {
						host.heldChoice = weekKey
					} else if (applyDelayFrames == 0) {
						chosenWeek.value = weekKey
					} else {
						scope.launch {
							repeat(applyDelayFrames) { withFrameNanos { } }
							chosenWeek.value = weekKey
						}
					}
				},
				onEvaluationClick = { _, _, _ -> },
				onEvaluationEdit = {},
				onEvaluationDelete = {},
				onEvaluationArrivalTaken = { evaluationId -> host.takenArrivals += evaluationId }
			),
			arrivalEvaluationId = arrivalEvaluationId.value
		)
	}

	return host
}

internal fun evaluationWeeksFixture(
	count: Int,
	cardsPerWeek: Int,
	emptyWeeks: Set<Int> = emptySet()
): List<EvaluationsWeekGroupItem> = (1..count).map { number ->
	EvaluationsWeekGroupItem(
		key = EvaluationsWeekKey.Academic(number),
		title = "Semana $number",
		groups = if (number in emptyWeeks) {
			emptyList()
		} else {
			listOf(
				EvaluationsGroupItem(
					items = (1..cardsPerWeek).map { index ->
						evaluationItemFixture(evaluationId = "week-$number-evaluation-$index")
					}
				)
			)
		}
	)
}

// A drag of the list: [steps] scroll steps of [stepPx] pixels, each one frame long.
internal class DragRequest(
	val steps: Int,
	val stepPx: Float
)

// What the list under a host reported and took, and the list itself.
internal class WeekListHost(
	val listState: LazyListState,
	val reportedWeeks: MutableList<EvaluationsWeekKey> = mutableListOf(),
	val takenArrivals: MutableList<String> = mutableListOf()
) {
	// While the host holds its choices, each choice the list makes is kept here instead of being applied: the last one
	// is what the host takes when it releases them.
	var isHoldingChoices = false
	var heldChoice: EvaluationsWeekKey? = null
}
