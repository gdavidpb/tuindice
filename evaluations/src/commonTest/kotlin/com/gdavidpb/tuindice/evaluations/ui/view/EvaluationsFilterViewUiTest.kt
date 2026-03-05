package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.testing.uiAvailableFilters
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class EvaluationsFilterViewUiTest {
	@Test
	fun when_filterChipTapped_then_invokesCheckedChangeCallback() = runTuIndiceUiTest {
		val availableFilters = uiAvailableFilters()
		val selectedEvents = mutableListOf<Pair<EvaluationFilter, Boolean>>()

		setTuIndiceTestContent {
			EvaluationFilterView(
				availableFilters = availableFilters,
				activeFilters = listOf(availableFilters.first()),
				onFilterCheckedChange = { filter, checked ->
					selectedEvents += filter to checked
				}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationsFiltersContainer)

		onNodeWithTag(
			EvaluationsUiTags.filterChip(availableFilters[1].getLabel())
		).performClick()

		assertEquals(1, selectedEvents.size)
		assertEquals(availableFilters[1].getLabel(), selectedEvents.first().first.getLabel())
		assertEquals(true, selectedEvents.first().second)
	}

	@Test
	fun when_activeFilterChipTapped_then_invokesUncheckedEvent() = runTuIndiceUiTest {
		val availableFilters = uiAvailableFilters()
		val selectedFilter = availableFilters.first()
		val selectedEvents = mutableListOf<Pair<EvaluationFilter, Boolean>>()

		setTuIndiceTestContent {
			EvaluationFilterView(
				availableFilters = availableFilters,
				activeFilters = listOf(selectedFilter),
				onFilterCheckedChange = { filter, checked ->
					selectedEvents += filter to checked
				}
			)
		}

		onNodeWithTag(
			EvaluationsUiTags.filterChip(selectedFilter.getLabel())
		).performClick()

		assertEquals(1, selectedEvents.size)
		assertEquals(selectedFilter.getLabel(), selectedEvents.first().first.getLabel())
		assertEquals(false, selectedEvents.first().second)
	}
}
