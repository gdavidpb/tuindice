package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.testing.evaluationsContentState
import com.gdavidpb.tuindice.evaluations.testing.uiAvailableFilters
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class EvaluationsContentViewUiTest {
	@Test
	fun when_fabAndFilterActionsTapped_then_invokeCallbacks() = runTuIndiceUiTest {
		val selectedEvents = mutableListOf<Pair<EvaluationFilter, Boolean>>()
		val availableFilters = uiAvailableFilters()
		val state = evaluationsContentState(
			activeFilters = listOf(availableFilters.first())
		)
		var addClicks = 0
		var clearClicks = 0

		setTuIndiceTestContent {
			EvaluationsContentView(
				state = state,
				onAddEvaluationClick = { addClicks++ },
				onClearFiltersClick = { clearClicks++ },
				onFilterCheckedChange = { filter, checked ->
					selectedEvents += filter to checked
				},
				onEvaluationClick = {},
				onEvaluationEdit = {},
				onEvaluationDelete = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationsContentContainer)
		assertNodeVisible(EvaluationsUiTags.EvaluationsFiltersContainer)
		assertNodeVisible(EvaluationsUiTags.EvaluationsList)
		assertNodeVisible(EvaluationsUiTags.EvaluationsClearFiltersFab)
		assertNodeVisible(EvaluationsUiTags.EvaluationsAddFab)

		onNodeWithTag(EvaluationsUiTags.filterChip(availableFilters[1].getLabel())).performClick()
		onNodeWithTag(EvaluationsUiTags.EvaluationsClearFiltersFab).performClick()
		onNodeWithTag(EvaluationsUiTags.EvaluationsAddFab).performClick()

		assertEquals(1, selectedEvents.size)
		assertEquals(availableFilters[1], selectedEvents.first().first)
		assertTrue(selectedEvents.first().second)
		assertEquals(1, clearClicks)
		assertEquals(1, addClicks)
	}

	@Test
	fun when_hasEvaluationsAndNoActiveFilters_then_hidesClearFabAndShowsAddFab() = runTuIndiceUiTest {
		val state = evaluationsContentState(activeFilters = emptyList())

		setTuIndiceTestContent {
			EvaluationsContentView(
				state = state,
				onAddEvaluationClick = {},
				onClearFiltersClick = {},
				onFilterCheckedChange = { _, _ -> },
				onEvaluationClick = {},
				onEvaluationEdit = {},
				onEvaluationDelete = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationsList)
		assertNodeVisible(EvaluationsUiTags.EvaluationsAddFab)
		assertNodeHidden(EvaluationsUiTags.EvaluationsClearFiltersFab)
	}

	@Test
	fun when_hasNoEvaluations_then_displaysEmptyMatchAndHidesClearFab() = runTuIndiceUiTest {
		val state = evaluationsContentState(
			originalEvaluations = emptyList(),
			activeFilters = emptyList()
		)

		setTuIndiceTestContent {
			EvaluationsContentView(
				state = state,
				onAddEvaluationClick = {},
				onClearFiltersClick = {},
				onFilterCheckedChange = { _, _ -> },
				onEvaluationClick = {},
				onEvaluationEdit = {},
				onEvaluationDelete = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationsContentContainer)
		assertNodeVisible(BaseUiTags.EmptyViewContainer)
		assertNodeVisible(EvaluationsUiTags.EvaluationsAddFab)
		assertNodeHidden(EvaluationsUiTags.EvaluationsClearFiltersFab)
		assertNodeHidden(EvaluationsUiTags.EvaluationsList)
	}

	@Test
	fun when_addAndClearFilterActionsTapped_then_forwardsCallbacks() = runTuIndiceUiTest {
		val state = evaluationsContentState(
			activeFilters = listOf(uiAvailableFilters().first())
		)
		var addClicks = 0
		var clearClicks = 0

		setTuIndiceTestContent {
			EvaluationsContentView(
				state = state,
				onAddEvaluationClick = { addClicks++ },
				onClearFiltersClick = { clearClicks++ },
				onFilterCheckedChange = { _, _ -> },
				onEvaluationClick = {},
				onEvaluationEdit = {},
				onEvaluationDelete = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationsAddFab)
		assertNodeVisible(EvaluationsUiTags.EvaluationsClearFiltersFab)

		onNodeWithTag(EvaluationsUiTags.EvaluationsClearFiltersFab).performClick()
		onNodeWithTag(EvaluationsUiTags.EvaluationsAddFab).performClick()

		assertEquals(1, clearClicks)
		assertEquals(1, addClicks)
	}
}
