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
import com.gdavidpb.tuindice.base.ui.model.WeekSelectorTags
import com.gdavidpb.tuindice.base.ui.view.WeekSelectorItemView
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
			WeekSelectorItemView(
				labelText = UiText.Raw(WEEK_LABEL),
				isCurrent = false,
				isSelected = selectedState.value,
				tags = WEEK_SELECTOR_TAGS
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
				WeekSelectorItemView(
					labelText = UiText.Raw(WEEK_LABEL),
					isCurrent = false,
					isSelected = true,
					tags = WEEK_SELECTOR_TAGS
				)
				WeekSelectorItemView(
					labelText = UiText.Raw(WEEK_LABEL),
					isCurrent = true,
					isSelected = true,
					tags = WEEK_SELECTOR_TAGS
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
				WeekSelectorItemView(
					labelText = UiText.Raw(WEEK_LABEL),
					isCurrent = false,
					isSelected = false,
					tags = WEEK_SELECTOR_TAGS
				)
				WeekSelectorItemView(
					labelText = UiText.Raw(WEEK_LABEL),
					isCurrent = false,
					isSelected = false,
					tags = WEEK_SELECTOR_TAGS
				)
			}
		}

		val (otherWeekStart, currentWeekStart) = labelStarts(count = 2)

		assertEquals(otherWeekStart, currentWeekStart)
	}

	@Test
	fun when_aDayOfTheWeekIsToday_then_theWeekCountsAsCurrentByDefault() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Column {
				WeekSelectorItemView(
					labelText = UiText.Raw(WEEK_LABEL),
					isCurrent = false,
					isSelected = false,
					tags = WEEK_SELECTOR_TAGS
				)
				WeekSelectorItemView(
					labelText = UiText.Raw(WEEK_LABEL),
					isCurrent = true,
					isSelected = false,
					tags = WEEK_SELECTOR_TAGS
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

	private companion object {
		const val WEEK_LABEL = "Semana 8"

		// The dot (8 dp) and the gap that separates it from the label (8 dp).
		val CURRENT_MARK_ROOM = 16.dp
		val LAYOUT_TOLERANCE = 1.dp

		// Las mismas etiquetas que la pestaña de Evaluaciones da a su tira: la prueba las busca por ellas.
		val WEEK_SELECTOR_TAGS = WeekSelectorTags(
			label = EvaluationsUiTags.EvaluationsWeekLabel,
			currentIndicator = EvaluationsUiTags.EvaluationsWeekCurrentIndicator
		)
	}
}
