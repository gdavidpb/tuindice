package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
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

	// A guard: a drag cutting the placement of the chosen week has always left the week to be placed on release; it must
	// go on doing so.
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

	// A guard: the student leaves week 12 for week 11 and releases before the choice of week 11 reaches the list. The list
	// stays where the student released it, frame after frame, and never goes back toward week 12.
	@Test
	fun when_theChoiceArrivesAfterTheStudentLeftTheWeek_then_theListStaysWhereTheStudentReleasedIt() = runTuIndiceUiTest {
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
		runOnIdle { drag.value = DragRequest(steps = 3, stepPx = -100f) }
		advanceFrames(RELEASE_FRAMES)

		assertEquals<List<EvaluationsWeekKey>>(listOf(EvaluationsWeekKey.Academic(11)), host.reportedWeeks)
		assertEquals(EvaluationsWeekKey.Academic(12), chosenWeek.value)
		assertTrue(!host.listState.isScrollInProgress, "the student has released the list")

		val released = host.listState.position()

		repeat(CHOICE_DELAY_FRAMES + 6) {
			advanceFrames(1)
			assertEquals(released, host.listState.position())
		}

		mainClock.autoAdvance = true
		waitForIdle()

		assertEquals(EvaluationsWeekKey.Academic(11), chosenWeek.value)
		assertEquals(released, host.listState.position())
		assertTrue(host.listState.firstVisibleItemIndex in WEEK_11_HEADER_INDEX until WEEK_12_HEADER_INDEX)
	}

	@Test
	fun when_theListCrossesTwoWeeksBeforeTheFirstChoiceArrives_then_theListStaysWhereTheStudentReleasedIt() =
		runTuIndiceUiTest {
			val chosenWeek = mutableStateOf<EvaluationsWeekKey>(EvaluationsWeekKey.Academic(10))
			val drag = mutableStateOf<DragRequest?>(null)
			val host = setWeekListHost(
				weeks = evaluationWeeksFixture(count = 12, cardsPerWeek = 4),
				chosenWeek = chosenWeek,
				drag = drag,
				applyDelayFrames = LONG_CHOICE_DELAY_FRAMES
			)
			waitForIdle()

			mainClock.autoAdvance = false
			runOnIdle { drag.value = DragRequest(steps = TWO_WEEKS_STEPS, stepPx = TWO_WEEKS_STEP_PX) }
			advanceFrames(TWO_WEEKS_STEPS + RELEASE_FRAMES)

			// Both weeks were crossed and reported, and no choice has reached the list yet.
			assertEquals<List<EvaluationsWeekKey>>(
				listOf(EvaluationsWeekKey.Academic(11), EvaluationsWeekKey.Academic(12)),
				host.reportedWeeks
			)
			assertEquals(EvaluationsWeekKey.Academic(10), chosenWeek.value)
			assertTrue(!host.listState.isScrollInProgress, "the student has released the list")

			val released = host.listState.position()

			// The first choice is applied by now, and so is the second.
			repeat(LONG_CHOICE_DELAY_FRAMES + 10) {
				advanceFrames(1)
				assertEquals(released, host.listState.position())
			}

			mainClock.autoAdvance = true
			waitForIdle()

			assertEquals(EvaluationsWeekKey.Academic(12), chosenWeek.value)
			assertEquals(released, host.listState.position())
			assertEquals<List<EvaluationsWeekKey>>(
				listOf(EvaluationsWeekKey.Academic(11), EvaluationsWeekKey.Academic(12)),
				host.reportedWeeks
			)
		}

	@Test
	fun when_theFirstWeekIsPlacedWithoutAScroll_then_theNextWeekChosenIsReachedByAnAnimation() = runTuIndiceUiTest {
		val chosenWeek = mutableStateOf<EvaluationsWeekKey>(EvaluationsWeekKey.Academic(1))
		val host = setWeekListHost(evaluationWeeksFixture(count = 12, cardsPerWeek = 4), chosenWeek, mutableStateOf(null))
		waitForIdle()

		// The list already stands on the first header: there was nothing to scroll to, yet the week was placed.
		assertEquals(0, host.listState.firstVisibleItemIndex)

		mainClock.autoAdvance = false
		runOnIdle { chosenWeek.value = EvaluationsWeekKey.Academic(6) }

		val seen = mutableSetOf<Int>()

		repeat(ANIMATION_FRAMES) {
			advanceFrames(1)
			seen += host.listState.firstVisibleItemIndex
		}

		mainClock.autoAdvance = true
		waitForIdle()

		assertHeaderAtTopOfList(6)
		assertTrue(seen.any { index -> index in 1 until WEEK_6_HEADER_INDEX }, "the list passed through $seen on its way")
	}

	// A guard: an arrival that placed the first week made the next change of week animate before, and it must go on so.
	@Test
	fun when_anArrivalPlacedTheFirstWeek_then_theNextWeekChosenIsReachedByAnAnimation() = runTuIndiceUiTest {
		val chosenWeek = mutableStateOf<EvaluationsWeekKey>(EvaluationsWeekKey.Academic(10))
		val host = setWeekListHost(
			weeks = evaluationWeeksFixture(count = 12, cardsPerWeek = 12),
			chosenWeek = chosenWeek,
			drag = mutableStateOf(null),
			arrivalEvaluationId = mutableStateOf<String?>("week-10-evaluation-12")
		)
		waitForIdle()

		mainClock.autoAdvance = false
		runOnIdle { chosenWeek.value = EvaluationsWeekKey.Academic(3) }

		val seen = mutableSetOf<Int>()

		repeat(ANIMATION_FRAMES) {
			advanceFrames(1)
			seen += host.listState.firstVisibleItemIndex
		}

		mainClock.autoAdvance = true
		waitForIdle()

		assertHeaderAtTopOfList(3)
		assertTrue(
			seen.any { index -> index in WEEK_3_HEADER_INDEX + 1 until WEEK_10_HEADER_INDEX },
			"the list passed through $seen on its way"
		)
	}

	@Test
	fun when_aPlacedWeekWithoutEvaluationsGainsThem_then_itIsPlacedOnItsHeader() = runTuIndiceUiTest {
		val chosenWeek = mutableStateOf<EvaluationsWeekKey>(EvaluationsWeekKey.Academic(6))
		val weeks = mutableStateOf(evaluationWeeksFixture(count = 12, cardsPerWeek = 4, emptyWeeks = setOf(6)))

		setWeekListHost(weeks = weeks, chosenWeek = chosenWeek, drag = mutableStateOf(null))
		waitForIdle()
		assertNodeHidden(EvaluationsUiTags.evaluationsWeekHeader(6))

		runOnIdle { weeks.value = evaluationWeeksFixture(count = 12, cardsPerWeek = 4) }
		waitForIdle()

		assertHeaderAtTopOfList(6)
		assertEquals(EvaluationsWeekKey.Academic(6), chosenWeek.value)
	}

	@Test
	fun when_anArrivalMeetsAWeekWithoutEvaluations_then_itIsTakenAndNoLaterWeekIsPlacedOnIt() = runTuIndiceUiTest {
		val arrival = "week-10-evaluation-12"
		val chosenWeek = mutableStateOf<EvaluationsWeekKey>(EvaluationsWeekKey.Academic(6))
		val host = setWeekListHost(
			weeks = evaluationWeeksFixture(count = 12, cardsPerWeek = 12, emptyWeeks = setOf(6)),
			chosenWeek = chosenWeek,
			drag = mutableStateOf(null),
			arrivalEvaluationId = mutableStateOf<String?>(arrival)
		)
		waitForIdle()

		assertEquals(listOf(arrival), host.takenArrivals)

		// Week 10 is the ninth week with evaluations of the list, and week 6 lists none: its header is item 104.
		runOnIdle { chosenWeek.value = EvaluationsWeekKey.Academic(10) }
		waitForIdle()

		assertEquals(WEEK_10_HEADER_INDEX_OF_TWELVE_CARDS, host.listState.firstVisibleItemIndex)
		assertEquals(0, host.listState.firstVisibleItemScrollOffset)
	}

	// A drag cuts the move to the chosen week while it is under way, and the arrival comes during the drag. The arrival is
	// placed on release, with its card in view, and the choice stays. The cut itself is a guard; the move being under way
	// to be cut needs the first week to count as placed, so it fails without that.
	@Test
	fun when_aDragCutsTheMoveToTheChosenWeekAndAnArrivalComesDuringIt_then_theArrivalIsPlacedOnRelease() =
		runTuIndiceUiTest {
			val arrival = "week-10-evaluation-12"
			val chosenWeek = mutableStateOf<EvaluationsWeekKey>(EvaluationsWeekKey.Academic(1))
			val drag = mutableStateOf<DragRequest?>(null)
			val arrivalEvaluationId = mutableStateOf<String?>(null)
			val host = setWeekListHost(
				weeks = evaluationWeeksFixture(count = 12, cardsPerWeek = 12),
				chosenWeek = chosenWeek,
				drag = drag,
				arrivalEvaluationId = arrivalEvaluationId
			)
			waitForIdle()

			mainClock.autoAdvance = false
			runOnIdle { chosenWeek.value = EvaluationsWeekKey.Academic(10) }
			advanceFrames(MOVE_FRAMES_BEFORE_DRAG)

			// The move to week 10 is under way: the list is neither on week 1 nor on week 10.
			assertTrue(host.listState.firstVisibleItemIndex in 1 until WEEK_10_HEADER_INDEX, "the list is on its way")

			runOnIdle { drag.value = DragRequest(steps = CUT_DRAG_STEPS, stepPx = CUT_DRAG_STEP_PX) }
			advanceFrames(2)
			runOnIdle { arrivalEvaluationId.value = arrival }
			advanceFrames(CUT_DRAG_STEPS + RELEASE_FRAMES)
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

	// Where the list stands: the first item in view and how far it is scrolled.
	private fun LazyListState.position(): Pair<Int, Int> = firstVisibleItemIndex to firstVisibleItemScrollOffset

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
	// by the list is applied after [applyDelayFrames] frames, each one on its own, so several can be on their way at once;
	// a drag requested through [drag] is made by the list.
	private fun ComposeUiTest.setWeekListHost(
		weeks: List<EvaluationsWeekGroupItem>,
		chosenWeek: MutableState<EvaluationsWeekKey>,
		drag: MutableState<DragRequest?>,
		arrivalEvaluationId: MutableState<String?> = mutableStateOf(null),
		applyDelayFrames: Int = 0
	): WeekListHost = setWeekListHost(mutableStateOf(weeks), chosenWeek, drag, arrivalEvaluationId, applyDelayFrames)

	// The same, with the weeks the list shows held in a state, so that they can change under it.
	private fun ComposeUiTest.setWeekListHost(
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

						if (applyDelayFrames == 0) {
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
		const val CHOICE_DELAY_FRAMES = 20
		const val LONG_CHOICE_DELAY_FRAMES = 40
		const val RELEASE_FRAMES = 8
		const val TWO_WEEKS_STEPS = 12
		const val TWO_WEEKS_STEP_PX = 300f
		const val ANIMATION_FRAMES = 60
		const val MOVE_FRAMES_BEFORE_DRAG = 5
		const val CUT_DRAG_STEPS = 10
		const val CUT_DRAG_STEP_PX = 20f
		const val WEEK_3_HEADER_INDEX = 26
		const val WEEK_6_HEADER_INDEX = 25
		const val WEEK_10_HEADER_INDEX = 117
		const val WEEK_11_HEADER_INDEX = 50
		const val WEEK_12_HEADER_INDEX = 55
		const val WEEK_10_HEADER_INDEX_OF_TWELVE_CARDS = 104
	}
}
