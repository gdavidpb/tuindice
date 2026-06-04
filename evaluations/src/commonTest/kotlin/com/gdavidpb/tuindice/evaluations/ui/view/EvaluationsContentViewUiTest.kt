package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.evaluations.testing.evaluationsContentState
import com.gdavidpb.tuindice.evaluations.testing.uiAvailableFilters
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class EvaluationsContentViewUiTest {
	@Test
	fun when_fabTapped_then_invokeCallback() = runTuIndiceUiTest {
		val state = evaluationsContentState()
		var addClicks = 0

		setTuIndiceTestContent {
			EvaluationsContentView(
				state = state,
				onAddEvaluationClick = { addClicks++ },
				onClearFiltersClick = {},
				onFilterCheckedChange = { _, _ -> },
				onEvaluationClick = { _, _, _ -> },
				onEvaluationEdit = {},
				onEvaluationDelete = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationsContentContainer)
		assertNodeVisible(EvaluationsUiTags.EvaluationsWeekStrip)
		assertNodeVisible(EvaluationsUiTags.EvaluationsList)
		assertNodeHidden(EvaluationsUiTags.EvaluationsFiltersContainer)
		assertNodeHidden(EvaluationsUiTags.EvaluationsClearFiltersFab)
		assertNodeVisible(EvaluationsUiTags.EvaluationsAddFab)

		onNodeWithTag(EvaluationsUiTags.EvaluationsAddFab).performClick()

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
				onEvaluationClick = { _, _, _ -> },
				onEvaluationEdit = {},
				onEvaluationDelete = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationsList)
		assertNodeVisible(EvaluationsUiTags.EvaluationsWeekStrip)
		assertNodeVisible(EvaluationsUiTags.EvaluationsAddFab)
		assertNodeHidden(EvaluationsUiTags.EvaluationsFiltersContainer)
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
				onEvaluationClick = { _, _, _ -> },
				onEvaluationEdit = {},
				onEvaluationDelete = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationsContentContainer)
		assertNodeVisible(EvaluationsUiTags.EvaluationsWeekStrip)
		assertNodeVisible(BaseUiTags.EmptyViewContainer)
		assertNodeVisible(EvaluationsUiTags.EvaluationsAddFab)
		assertNodeHidden(EvaluationsUiTags.EvaluationsClearFiltersFab)
		assertNodeHidden(EvaluationsUiTags.EvaluationsFiltersContainer)
		assertNodeHidden(EvaluationsUiTags.EvaluationsList)
	}

	@Test
	fun when_activeFiltersExist_then_filterControlsStayHidden() = runTuIndiceUiTest {
		val state = evaluationsContentState(
			activeFilters = listOf(uiAvailableFilters().first())
		)
		var addClicks = 0

		setTuIndiceTestContent {
			EvaluationsContentView(
				state = state,
				onAddEvaluationClick = { addClicks++ },
				onClearFiltersClick = {},
				onFilterCheckedChange = { _, _ -> },
				onEvaluationClick = { _, _, _ -> },
				onEvaluationEdit = {},
				onEvaluationDelete = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationsAddFab)
		assertNodeHidden(EvaluationsUiTags.EvaluationsFiltersContainer)
		assertNodeHidden(EvaluationsUiTags.EvaluationsClearFiltersFab)

		onNodeWithTag(EvaluationsUiTags.EvaluationsAddFab).performClick()

		assertEquals(1, addClicks)
	}
}
