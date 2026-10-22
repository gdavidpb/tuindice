package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.testing.evaluationItemFixture
import com.gdavidpb.tuindice.evaluations.testing.evaluationsGroupItemsFixture
import com.gdavidpb.tuindice.evaluations.testing.evaluationsWeekGroupItemsFixture
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.evaluations.ui.model.EvaluationsActions
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.coroutines.flow.first
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class EvaluationsViewUiTest {
	@Test
	fun when_gradeButtonTapped_then_invokesEvaluationClickCallback() = runTuIndiceUiTest {
		val groups = evaluationsGroupItemsFixture()
		var clickedEvaluationId: String? = null
		var clickedEvaluationName: String? = null
		var clickedSubjectCode: String? = null

		setTuIndiceTestContent {
			EvaluationsView(
				lazyListState = rememberLazyListState(),
				weekGroups = evaluationsWeekGroupItemsFixture(groups),
				selectedWeekKey = EvaluationsWeekKey.Academic(8),
				actions = EvaluationsActions(
					onWeekClick = {},
					onEvaluationClick = { evaluationId, evaluationName, subjectCode ->
						clickedEvaluationId = evaluationId
						clickedEvaluationName = evaluationName
						clickedSubjectCode = subjectCode
					},
					onEvaluationEdit = {},
					onEvaluationDelete = {}
				)
			)
		}

		val item = groups.first().items.first()

		assertNodeVisible(EvaluationsUiTags.EvaluationsList)
		assertNodeVisible(EvaluationsUiTags.evaluationsWeekHeader(8))
		assertNodeVisible(EvaluationsUiTags.evaluationHeader("Semana 8"))
		assertNodeVisible(EvaluationsUiTags.evaluationItemCard(item.evaluationId))
		onNodeWithText("1 evaluación").assertIsDisplayed()

		onNodeWithTag(EvaluationsUiTags.evaluationGradeActionButton(item.evaluationId)).performClick()

		assertEquals(item.evaluationId, clickedEvaluationId)
		assertEquals(item.nameText, clickedEvaluationName)
		assertEquals(item.subjectCodeText, clickedSubjectCode)
	}

	@Test
	fun when_itemIsNotClickable_then_gradeButtonTapDoesNotInvokeEvaluationCallback() = runTuIndiceUiTest {
		val sourceGroups = evaluationsGroupItemsFixture()
		val disabledItem = sourceGroups.first().items.first().copy(
			evaluationId = "evaluation-item-disabled",
			isClickable = false
		)
		val groups = listOf(
			sourceGroups.first().copy(items = listOf(disabledItem))
		)
		var clickedEvaluationId: String? = null

		setTuIndiceTestContent {
			EvaluationsView(
				lazyListState = rememberLazyListState(),
				weekGroups = evaluationsWeekGroupItemsFixture(groups),
				selectedWeekKey = EvaluationsWeekKey.Academic(8),
				actions = EvaluationsActions(
					onWeekClick = {},
					onEvaluationClick = { evaluationId, _, _ -> clickedEvaluationId = evaluationId },
					onEvaluationEdit = {},
					onEvaluationDelete = {}
				)
			)
		}

		onNodeWithTag(EvaluationsUiTags.evaluationGradeActionButton(disabledItem.evaluationId)).performClick()

		assertEquals(null, clickedEvaluationId)
	}

	@Test
	fun when_weekHasNoEvaluations_then_hidesWeekHeader() = runTuIndiceUiTest {
		val groups = evaluationsGroupItemsFixture()
		val weekGroups = listOf(
			EvaluationsWeekGroupItem(
				key = EvaluationsWeekKey.Academic(7),
				title = "Semana 7",
				groups = emptyList()
			),
			evaluationsWeekGroupItemsFixture(groups).first()
		)

		setTuIndiceTestContent {
			EvaluationsView(
				lazyListState = rememberLazyListState(),
				weekGroups = weekGroups,
				selectedWeekKey = EvaluationsWeekKey.Academic(7),
				actions = EvaluationsActions(
					onWeekClick = {},
					onEvaluationClick = { _, _, _ -> },
					onEvaluationEdit = {},
					onEvaluationDelete = {}
				)
			)
		}

		assertNodeHidden(EvaluationsUiTags.evaluationsWeekHeader(7))
		assertNodeVisible(EvaluationsUiTags.evaluationsWeekHeader(8))
		assertNodeVisible(EvaluationsUiTags.evaluationItemCard(groups.first().items.first().evaluationId))
	}

	@Test
	fun when_continuousGroupExists_then_showsContinuousHeader() = runTuIndiceUiTest {
		val groups = evaluationsGroupItemsFixture()
		val weekGroupsFixture = groups.map { group ->
			group.copy(
				items = group.items.map { item ->
					item.copy(evaluationId = "${item.evaluationId}-week")
				}
			)
		}
		val weekGroups = listOf(
			EvaluationsWeekGroupItem(
				key = EvaluationsWeekKey.Continuous,
				title = "Continuas",
				groups = groups
			),
			EvaluationsWeekGroupItem(
				key = EvaluationsWeekKey.Academic(8),
				title = "Semana 8",
				groups = weekGroupsFixture
			)
		)

		setTuIndiceTestContent {
			EvaluationsView(
				lazyListState = rememberLazyListState(),
				weekGroups = weekGroups,
				selectedWeekKey = EvaluationsWeekKey.Continuous,
				actions = EvaluationsActions(
					onWeekClick = {},
					onEvaluationClick = { _, _, _ -> },
					onEvaluationEdit = {},
					onEvaluationDelete = {}
				)
			)
		}

		assertNodeVisible(EvaluationsUiTags.evaluationsWeekHeader(EvaluationsWeekKey.Continuous))
		assertNodeVisible(EvaluationsUiTags.evaluationHeader("Continuas"))
	}

	@Test
	fun when_arrivalIsGivenBelowTheViewport_then_itsCardIsBroughtIntoViewAndTheListTakesIt() = runTuIndiceUiTest {
		val weeks = (1..12).map { number ->
			EvaluationsWeekGroupItem(
				key = EvaluationsWeekKey.Academic(number),
				title = "Semana $number",
				groups = listOf(
					EvaluationsGroupItem(
						items = (1..12).map { index ->
							evaluationItemFixture(evaluationId = "week-$number-evaluation-$index")
						}
					)
				)
			)
		}
		val arrival = "week-10-evaluation-12"
		var taken: String? = null

		setTuIndiceTestContent {
			EvaluationsView(
				lazyListState = rememberLazyListState(),
				weekGroups = weeks,
				selectedWeekKey = EvaluationsWeekKey.Academic(10),
				actions = EvaluationsActions(
					onWeekClick = {},
					onEvaluationClick = { _, _, _ -> },
					onEvaluationEdit = {},
					onEvaluationDelete = {},
					onEvaluationArrivalTaken = { evaluationId -> taken = evaluationId }
				),
				arrivalEvaluationId = arrival
			)
		}

		waitForIdle()
		assertNodeVisible(EvaluationsUiTags.evaluationItemCard(arrival))
		assertEquals(arrival, taken)
	}

	@Test
	fun when_theLastWeekIsSelectedOnTheFirstLayout_then_itsHeaderReachesTheTopOfTheList() = runTuIndiceUiTest {
		val earlier = evaluationItemFixture(evaluationId = "week-7-evaluation")
		val last = evaluationItemFixture(evaluationId = "week-8-evaluation")
		val weekGroups = listOf(
			EvaluationsWeekGroupItem(
				key = EvaluationsWeekKey.Academic(7),
				title = "Semana 7",
				groups = listOf(EvaluationsGroupItem(items = listOf(earlier)))
			),
			EvaluationsWeekGroupItem(
				key = EvaluationsWeekKey.Academic(8),
				title = "Semana 8",
				groups = listOf(EvaluationsGroupItem(items = listOf(last)))
			)
		)

		// The last week is far shorter than the list: only the room left at the end lets its header go up.
		setTuIndiceTestContent {
			Box(modifier = Modifier.height(640.dp)) {
				EvaluationsView(
					lazyListState = rememberLazyListState(),
					weekGroups = weekGroups,
					selectedWeekKey = EvaluationsWeekKey.Academic(8),
					actions = EvaluationsActions(
						onWeekClick = {},
						onEvaluationClick = { _, _, _ -> },
						onEvaluationEdit = {},
						onEvaluationDelete = {}
					)
				)
			}
		}

		waitForIdle()

		val list = onNodeWithTag(EvaluationsUiTags.EvaluationsList).getUnclippedBoundsInRoot()
		val header = onNodeWithTag(EvaluationsUiTags.evaluationsWeekHeader(8)).getUnclippedBoundsInRoot()

		assertTrue(
			abs((header.top - list.top).value) < 1f,
			"the header of the last week at ${header.top} is at the top of the list at ${list.top}"
		)
		assertNodeHidden(EvaluationsUiTags.evaluationsWeekHeader(7))
	}

	@Test
	fun when_aWeekIsChosenAndTheListIsDraggedBeforeItStandsOnIt_then_theChosenWeekStaysAndItsContentIsInView() =
		runTuIndiceUiTest {
			val weeks = evaluationWeeksFixture(count = 12, cardsPerWeek = 4)
			val chosenWeek = mutableStateOf<EvaluationsWeekKey>(EvaluationsWeekKey.Academic(12))
			val reportedWeeks = mutableListOf<EvaluationsWeekKey>()
			val isDragArmed = mutableStateOf(false)

			setTuIndiceTestContent {
				val lazyListState = rememberLazyListState()

				// The week is chosen as soon as the student's drag of the list starts: the list is not on it yet.
				LaunchedEffect(lazyListState) {
					snapshotFlow { isDragArmed.value && lazyListState.isScrollInProgress }
						.first { isDragging -> isDragging }

					chosenWeek.value = EvaluationsWeekKey.Academic(11)
				}

				EvaluationsView(
					lazyListState = lazyListState,
					weekGroups = weeks,
					selectedWeekKey = chosenWeek.value,
					actions = EvaluationsActions(
						onWeekClick = { weekKey ->
							reportedWeeks += weekKey
							chosenWeek.value = weekKey
						},
						onEvaluationClick = { _, _, _ -> },
						onEvaluationEdit = {},
						onEvaluationDelete = {}
					)
				)
			}

			waitForIdle()

			runOnIdle {
				isDragArmed.value = true
			}
			onNodeWithTag(EvaluationsUiTags.EvaluationsList).performTouchInput { swipeUp() }
			waitForIdle()

			assertEquals(emptyList<EvaluationsWeekKey>(), reportedWeeks)
			assertEquals(EvaluationsWeekKey.Academic(11), chosenWeek.value)
			assertNodeVisible(EvaluationsUiTags.evaluationItemCard("week-11-evaluation-1"))

			val list = onNodeWithTag(EvaluationsUiTags.EvaluationsList).getUnclippedBoundsInRoot()
			val header = onNodeWithTag(EvaluationsUiTags.evaluationsWeekHeader(11)).getUnclippedBoundsInRoot()

			assertTrue(
				abs((header.top - list.top).value) < 1f,
				"the chosen week at ${header.top} is at the top of the list at ${list.top}"
			)
		}

	@Test
	fun when_theChosenWeekIsPlaced_then_aDragOfTheListChoosesTheWeeksItShows() = runTuIndiceUiTest {
		val chosenWeek = mutableStateOf<EvaluationsWeekKey>(EvaluationsWeekKey.Academic(10))
		val drag = mutableStateOf<DragRequest?>(null)
		val host = setWeekListHost(evaluationWeeksFixture(count = 12, cardsPerWeek = 4), chosenWeek, drag)
		waitForIdle()

		// The list is on week 10: a long drag forward shows week 11, then week 12, and each is chosen as it shows.
		runOnIdle { drag.value = DragRequest(steps = FORWARD_STEPS, stepPx = FORWARD_STEP_PX) }
		waitForIdle()

		assertEquals<List<EvaluationsWeekKey>>(
			listOf(EvaluationsWeekKey.Academic(11), EvaluationsWeekKey.Academic(12)),
			host.reportedWeeks
		)
		assertEquals(EvaluationsWeekKey.Academic(12), chosenWeek.value)
	}

	@Test
	fun when_aDragCutsThePlacementOfTheChosenWeek_then_theWeekIsPlacedOnReleaseAndTheChoiceStays() = runTuIndiceUiTest {
		val chosenWeek = mutableStateOf<EvaluationsWeekKey>(EvaluationsWeekKey.Academic(12))
		val drag = mutableStateOf<DragRequest?>(null)
		val host = setWeekListHost(evaluationWeeksFixture(count = 12, cardsPerWeek = 4), chosenWeek, drag)
		waitForIdle()

		mainClock.autoAdvance = false
		runOnIdle { chosenWeek.value = EvaluationsWeekKey.Academic(11) }
		advanceFrames(2)
		runOnIdle { drag.value = DragRequest(steps = 5, stepPx = 40f) }
		advanceFrames(10)
		mainClock.autoAdvance = true
		waitForIdle()

		assertEquals(emptyList<EvaluationsWeekKey>(), host.reportedWeeks)
		assertEquals(EvaluationsWeekKey.Academic(11), chosenWeek.value)
		assertHeaderAtTopOfList(11)
	}

	@Test
	fun when_theChosenWeekIsPlacedAndADragCutsItTwice_then_theWeekIsPlacedAndALaterDragChoosesAgain() =
		runTuIndiceUiTest {
			val chosenWeek = mutableStateOf<EvaluationsWeekKey>(EvaluationsWeekKey.Academic(12))
			val drag = mutableStateOf<DragRequest?>(null)
			val host = setWeekListHost(evaluationWeeksFixture(count = 12, cardsPerWeek = 4), chosenWeek, drag)
			waitForIdle()

			mainClock.autoAdvance = false
			runOnIdle { chosenWeek.value = EvaluationsWeekKey.Academic(11) }
			advanceFrames(2)
			runOnIdle { drag.value = DragRequest(steps = 4, stepPx = 40f) }
			advanceFrames(8)
			advanceFrames(2)
			runOnIdle { drag.value = DragRequest(steps = 4, stepPx = -40f) }
			advanceFrames(8)
			mainClock.autoAdvance = true
			waitForIdle()

			assertEquals(emptyList<EvaluationsWeekKey>(), host.reportedWeeks)
			assertEquals(EvaluationsWeekKey.Academic(11), chosenWeek.value)
			assertHeaderAtTopOfList(11)

			runOnIdle { drag.value = DragRequest(steps = FORWARD_STEPS, stepPx = FORWARD_STEP_PX) }
			waitForIdle()

			assertEquals<List<EvaluationsWeekKey>>(listOf(EvaluationsWeekKey.Academic(12)), host.reportedWeeks)
			assertEquals(EvaluationsWeekKey.Academic(12), chosenWeek.value)
		}

	@Test
	fun when_aDragFollowsAChoiceOfAWeekWithoutEvaluations_then_theDragDoesNotRewriteTheChoice() = runTuIndiceUiTest {
		val chosenWeek = mutableStateOf<EvaluationsWeekKey>(EvaluationsWeekKey.Academic(9))
		val drag = mutableStateOf<DragRequest?>(null)
		val weeks = evaluationWeeksFixture(count = 12, cardsPerWeek = 4, emptyWeeks = setOf(6))
		val host = setWeekListHost(weeks, chosenWeek, drag)
		waitForIdle()

		mainClock.autoAdvance = false
		runOnIdle { drag.value = DragRequest(steps = 30, stepPx = 25f) }
		advanceFrames(2)
		runOnIdle { chosenWeek.value = EvaluationsWeekKey.Academic(6) }
		advanceFrames(40)
		mainClock.autoAdvance = true
		waitForIdle()

		assertEquals(emptyList<EvaluationsWeekKey>(), host.reportedWeeks)
		assertEquals(EvaluationsWeekKey.Academic(6), chosenWeek.value)
	}

	@Test
	fun when_theChoiceArrivesAfterTheStudentLeftTheWeek_then_theListDoesNotJumpBackToTheOldWeek() = runTuIndiceUiTest {
		val chosenWeek = mutableStateOf<EvaluationsWeekKey>(EvaluationsWeekKey.Academic(12))
		val drag = mutableStateOf<DragRequest?>(null)
		val host = setWeekListHost(
			weeks = evaluationWeeksFixture(count = 12, cardsPerWeek = 4),
			chosenWeek = chosenWeek,
			drag = drag,
			applyDelayFrames = CHOICE_DELAY_FRAMES
		)
		waitForIdle()

		mainClock.autoAdvance = false
		// The student drags back into week 11, and releases before the choice has reached the list.
		runOnIdle { drag.value = DragRequest(steps = 3, stepPx = -100f) }
		advanceFrames(CHOICE_DELAY_FRAMES + 6)
		mainClock.autoAdvance = true
		waitForIdle()

		assertEquals<List<EvaluationsWeekKey>>(listOf(EvaluationsWeekKey.Academic(11)), host.reportedWeeks)
		assertEquals(EvaluationsWeekKey.Academic(11), chosenWeek.value)
		// Week 11 lists its header at index 50 and its four cards after it; week 12 starts at index 55.
		assertTrue(host.listState.firstVisibleItemIndex in WEEK_11_HEADER_INDEX until WEEK_12_HEADER_INDEX)
	}

	@Test
	fun when_anArrivalIsCutByADrag_then_itIsPlacedOnReleaseAndItsCardIsInView() = runTuIndiceUiTest {
		val arrival = "week-10-evaluation-12"
		val chosenWeek = mutableStateOf<EvaluationsWeekKey>(EvaluationsWeekKey.Academic(10))
		val drag = mutableStateOf<DragRequest?>(null)

		mainClock.autoAdvance = false
		val host = setWeekListHost(
			weeks = evaluationWeeksFixture(count = 12, cardsPerWeek = 12),
			chosenWeek = chosenWeek,
			drag = drag,
			arrivalEvaluationId = mutableStateOf<String?>(arrival)
		)
		advanceFrames(2)
		runOnIdle { drag.value = DragRequest(steps = 2, stepPx = 30f) }
		advanceFrames(6)
		mainClock.autoAdvance = true
		waitForIdle()

		assertEquals(listOf(arrival), host.takenArrivals)
		assertEquals(emptyList<EvaluationsWeekKey>(), host.reportedWeeks)
		assertEquals(EvaluationsWeekKey.Academic(10), chosenWeek.value)
		assertNodeVisible(EvaluationsUiTags.evaluationItemCard(arrival))
	}

	// A drag of the list by the student: a scroll with the user's priority, which cuts any move the list is making.
	private suspend fun LazyListState.dragBy(steps: Int, stepPx: Float) {
		scroll(MutatePriority.UserInput) {
			repeat(steps) {
				scrollBy(stepPx)
				withFrameNanos { }
			}
		}
	}

	private fun ComposeUiTest.advanceFrames(count: Int) {
		repeat(count) { mainClock.advanceTimeByFrame() }
	}

	private fun ComposeUiTest.assertHeaderAtTopOfList(weekNumber: Int) {
		val list = onNodeWithTag(EvaluationsUiTags.EvaluationsList).getUnclippedBoundsInRoot()
		val header = onNodeWithTag(EvaluationsUiTags.evaluationsWeekHeader(weekNumber)).getUnclippedBoundsInRoot()

		assertTrue(
			abs((header.top - list.top).value) < 1f,
			"the header of week $weekNumber at ${header.top} is at the top of the list at ${list.top}"
		)
	}

	// Sets the list under a host that keeps the chosen week, and returns what the list reported and took. A choice made
	// by the list is applied after [applyDelayFrames] frames; a drag requested through [drag] is made by the list.
	private fun ComposeUiTest.setWeekListHost(
		weeks: List<EvaluationsWeekGroupItem>,
		chosenWeek: MutableState<EvaluationsWeekKey>,
		drag: MutableState<DragRequest?>,
		arrivalEvaluationId: MutableState<String?> = mutableStateOf(null),
		applyDelayFrames: Int = 0
	): WeekListHost {
		val host = WeekListHost(listState = LazyListState())

		setTuIndiceTestContent {
			val requestedWeek = remember { mutableStateOf<EvaluationsWeekKey?>(null) }

			LaunchedEffect(drag.value) {
				drag.value?.let { request -> host.listState.dragBy(steps = request.steps, stepPx = request.stepPx) }
			}

			LaunchedEffect(requestedWeek.value) {
				val weekKey = requestedWeek.value ?: return@LaunchedEffect

				repeat(applyDelayFrames) { withFrameNanos { } }
				chosenWeek.value = weekKey
			}

			EvaluationsView(
				lazyListState = host.listState,
				weekGroups = weeks,
				selectedWeekKey = chosenWeek.value,
				actions = EvaluationsActions(
					onWeekClick = { weekKey ->
						host.reportedWeeks += weekKey

						if (applyDelayFrames == 0) {
							chosenWeek.value = weekKey
						} else {
							requestedWeek.value = weekKey
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

	private fun evaluationWeeksFixture(
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
	private class DragRequest(
		val steps: Int,
		val stepPx: Float
	)

	// What the list under a host reported and took, and the list itself.
	private class WeekListHost(
		val listState: LazyListState,
		val reportedWeeks: MutableList<EvaluationsWeekKey> = mutableListOf(),
		val takenArrivals: MutableList<String> = mutableListOf()
	)

	private companion object {
		const val FORWARD_STEPS = 60
		const val FORWARD_STEP_PX = 100f
		const val CHOICE_DELAY_FRAMES = 5
		const val WEEK_11_HEADER_INDEX = 50
		const val WEEK_12_HEADER_INDEX = 55
	}
}
