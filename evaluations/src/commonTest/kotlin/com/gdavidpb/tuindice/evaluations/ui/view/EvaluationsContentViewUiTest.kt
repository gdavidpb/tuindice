package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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
				hasEvaluations = true,
				emptyMatchTitle = "Sin coincidencias",
				emptyMatchMessage = "No hay evaluaciones para los filtros aplicados",
				onAddEvaluationClick = { addClicks++ },
				onClearFiltersClick = { clearClicks++ },
				onFilterCheckedChange = { filter, checked ->
					selectedEvents += filter to checked
				},
				onEvaluationClick = {},
				onEvaluationEdit = {},
				onEvaluationDelete = {},
				addFabContent = { Text("+") },
				clearFiltersFabContent = { Text("X") },
				emptyMatchHeaderContent = {},
				evaluationsContent = { _: LazyListState ->
					Text("Contenido evaluaciones")
				}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationsContentContainer)
		assertNodeVisible(EvaluationsUiTags.EvaluationsFiltersContainer)
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
				hasEvaluations = true,
				emptyMatchTitle = "Sin coincidencias",
				emptyMatchMessage = "No hay evaluaciones para los filtros aplicados",
				onAddEvaluationClick = {},
				onClearFiltersClick = {},
				onFilterCheckedChange = { _, _ -> },
				onEvaluationClick = {},
				onEvaluationEdit = {},
				onEvaluationDelete = {},
				addFabContent = { Text("+") },
				clearFiltersFabContent = { Text("X") },
				emptyMatchHeaderContent = {},
				evaluationsContent = { _: LazyListState ->
					Text("Contenido evaluaciones")
				}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationsAddFab)
		assertNodeHidden(EvaluationsUiTags.EvaluationsClearFiltersFab)
	}

	@Test
	fun when_hasNoEvaluations_then_displaysEmptyMatchAndHidesClearFab() = runTuIndiceUiTest {
		val state = evaluationsContentState(activeFilters = emptyList())
		var evaluationsContentCalls = 0

		setTuIndiceTestContent {
			EvaluationsContentView(
				state = state,
				hasEvaluations = false,
				emptyMatchTitle = "Sin coincidencias",
				emptyMatchMessage = "No hay evaluaciones para los filtros aplicados",
				onAddEvaluationClick = {},
				onClearFiltersClick = {},
				onFilterCheckedChange = { _, _ -> },
				onEvaluationClick = {},
				onEvaluationEdit = {},
				onEvaluationDelete = {},
				addFabContent = { Text("+") },
				clearFiltersFabContent = { Text("X") },
				emptyMatchHeaderContent = { Text("Header vacio") },
				evaluationsContent = { _: LazyListState ->
					evaluationsContentCalls++
				}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationsContentContainer)
		assertNodeVisible(BaseUiTags.EmptyViewContainer)
		assertNodeVisible(EvaluationsUiTags.EvaluationsAddFab)
		assertNodeHidden(EvaluationsUiTags.EvaluationsClearFiltersFab)
		onNodeWithText("Header vacio").assertIsDisplayed()
		assertEquals(0, evaluationsContentCalls)
	}

	@Test
	fun when_usingDefaultOverload_then_forwardsAddAndClearFilterCallbacks() = runTuIndiceUiTest {
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
