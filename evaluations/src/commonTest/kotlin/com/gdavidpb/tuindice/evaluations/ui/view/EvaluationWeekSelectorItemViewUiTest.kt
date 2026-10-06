package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationWeekDayItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Being selected only changes the weight and the colour of the label, and the mark of the current
// week is a dot without semantics: neither is read here. What the dot does to the layout is: it
// takes room beside the label, so the centred label starts earlier.
@OptIn(ExperimentalTestApi::class)
class EvaluationWeekSelectorItemViewUiTest {
	@Test
	fun when_rendered_then_labelReadsTheWeekText_whetherSelectedOrNot() = runTuIndiceUiTest {
		val selectedState = mutableStateOf(false)

		setTuIndiceTestContent {
			EvaluationWeekSelectorItemView(
				item = weekItem(isCurrent = false),
				isSelected = selectedState.value
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationsWeekLabel)
		onNodeWithTag(EvaluationsUiTags.EvaluationsWeekLabel).assertTextEquals(WEEK_LABEL)

		runOnIdle {
			selectedState.value = true
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationsWeekLabel).assertTextEquals(WEEK_LABEL)
	}

	@Test
	fun when_weekIsCurrent_then_itsMarkTakesRoomBesideTheLabel() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Column {
				EvaluationWeekSelectorItemView(
					item = weekItem(isCurrent = false),
					isSelected = true
				)
				EvaluationWeekSelectorItemView(
					item = weekItem(isCurrent = true),
					isSelected = true
				)
			}
		}

		val (otherWeekStart, currentWeekStart) = labelStarts(count = 2)

		assertCloseTo(
			expected = otherWeekStart - CURRENT_MARK_ROOM / 2,
			actual = currentWeekStart,
			message = "the label of the current week makes room for its mark"
		)
	}

	@Test
	fun when_currentMarkIsNotShown_then_theCurrentWeekIsLaidOutLikeAnyOther() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Column {
				EvaluationWeekSelectorItemView(
					item = weekItem(isCurrent = false),
					isSelected = false,
					showCurrentIndicator = false
				)
				EvaluationWeekSelectorItemView(
					item = weekItem(isCurrent = true),
					isSelected = false,
					showCurrentIndicator = false
				)
			}
		}

		val (otherWeekStart, currentWeekStart) = labelStarts(count = 2)

		assertEquals(otherWeekStart, currentWeekStart)
	}

	@Test
	fun when_aDayOfTheWeekIsToday_then_theWeekCountsAsCurrentByDefault() = runTuIndiceUiTest {
		val weekWithToday = EvaluationsWeekItem(
			key = EvaluationsWeekKey.Academic(8),
			labelText = WEEK_LABEL,
			days = listOf(
				EvaluationWeekDayItem(UiText.Raw("JUE"), "21", isSelected = true, hasEvaluations = false)
			)
		)

		setTuIndiceTestContent {
			Column {
				EvaluationWeekSelectorItemView(
					item = weekItem(isCurrent = false),
					isSelected = false
				)
				EvaluationWeekSelectorItemView(
					item = weekWithToday,
					isSelected = false
				)
			}
		}

		val (otherWeekStart, weekWithTodayStart) = labelStarts(count = 2)

		assertTrue(weekWithTodayStart < otherWeekStart, "a week holding today shows the mark of the current week")
	}

	private fun ComposeUiTest.labelStarts(count: Int): List<Dp> {
		val labels = onAllNodesWithTag(EvaluationsUiTags.EvaluationsWeekLabel)

		labels.assertCountEquals(count)

		return List(count) { index -> labels[index].getUnclippedBoundsInRoot().left }
	}

	private fun assertCloseTo(expected: Dp, actual: Dp, message: String) {
		val distance = if (expected > actual) expected - actual else actual - expected

		assertTrue(distance <= LAYOUT_TOLERANCE, "$message: expected $expected, was $actual")
	}

	private fun weekItem(isCurrent: Boolean) = EvaluationsWeekItem(
		key = EvaluationsWeekKey.Academic(8),
		labelText = WEEK_LABEL,
		days = emptyList(),
		isCurrent = isCurrent
	)

	private companion object {
		const val WEEK_LABEL = "Semana 8"

		// The dot (8 dp) and the gap that separates it from the label (8 dp).
		val CURRENT_MARK_ROOM = 16.dp
		val LAYOUT_TOLERANCE = 1.dp
	}
}
