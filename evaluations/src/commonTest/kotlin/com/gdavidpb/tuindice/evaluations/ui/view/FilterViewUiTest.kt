package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationStateFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationSubjectFilter
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toEvaluationFilterChipItem
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class FilterViewUiTest {
	@Test
	fun when_filterChipTapped_then_switchesSelectionState() = runTuIndiceUiTest {
		val pendingFilter = EvaluationStateFilter(label = "Pendientes") { true }
		val selectedEvents = mutableListOf<Pair<String, Boolean>>()

		setTuIndiceTestContent {
			FilterView(
				items = listOf(
					pendingFilter.toEvaluationFilterChipItem(isChecked = true)
				),
				onCheckedChange = { filter, checked ->
					selectedEvents += filter.getLabel() to checked
				}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationsFilterRow)
		assertNodeVisible(EvaluationsUiTags.filterChip("Pendientes"))

		onNodeWithTag(EvaluationsUiTags.filterChip("Pendientes")).performClick()

		assertEquals(listOf("Pendientes" to false), selectedEvents)
	}

	@Test
	fun when_unselectedFilterChipTapped_then_emitsCheckedState() = runTuIndiceUiTest {
		val completedFilter = EvaluationStateFilter(label = "Completadas") { true }
		val selectedEvents = mutableListOf<Pair<String, Boolean>>()

		setTuIndiceTestContent {
			FilterView(
				items = listOf(
					completedFilter.toEvaluationFilterChipItem(isChecked = false)
				),
				onCheckedChange = { filter, checked ->
					selectedEvents += filter.getLabel() to checked
				}
			)
		}

		onNodeWithTag(EvaluationsUiTags.filterChip("Completadas")).performClick()

		assertEquals(listOf("Completadas" to true), selectedEvents)
	}

	@Test
	fun when_subjectFilterChipTapped_then_emitsUncheckedState() = runTuIndiceUiTest {
		val subjectFilter = EvaluationSubjectFilter(subjectCode = "INF-101")
		val selectedEvents = mutableListOf<Pair<String, Boolean>>()

		setTuIndiceTestContent {
			FilterView(
				items = listOf(
					subjectFilter.toEvaluationFilterChipItem(isChecked = true)
				),
				onCheckedChange = { filter, checked ->
					selectedEvents += filter.getLabel() to checked
				}
			)
		}

		assertNodeVisible(EvaluationsUiTags.filterChip("INF-101"))
		assertNodeVisible(
			EvaluationsUiTags.filterChipCheck("INF-101"),
			useUnmergedTree = true
		)
		onNodeWithTag(EvaluationsUiTags.filterChip("INF-101")).performClick()

		assertEquals(listOf("INF-101" to false), selectedEvents)
	}
}
