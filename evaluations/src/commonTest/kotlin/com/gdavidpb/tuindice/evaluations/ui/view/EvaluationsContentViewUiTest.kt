package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.evaluations.testing.evaluationsContentState
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
				onEvaluationClick = { _, _, _ -> },
				onEvaluationEdit = {},
				onEvaluationDelete = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationsContentContainer)
		assertNodeVisible(EvaluationsUiTags.EvaluationsWeekStrip)
		assertNodeVisible(EvaluationsUiTags.EvaluationsList)
		assertNodeVisible(EvaluationsUiTags.EvaluationsAddFab)

		onNodeWithTag(EvaluationsUiTags.EvaluationsAddFab).performClick()

		assertEquals(1, addClicks)
	}

	@Test
	fun when_hasEvaluations_then_showsAddFab() = runTuIndiceUiTest {
		val state = evaluationsContentState()

		setTuIndiceTestContent {
			EvaluationsContentView(
				state = state,
				onAddEvaluationClick = {},
				onEvaluationClick = { _, _, _ -> },
				onEvaluationEdit = {},
				onEvaluationDelete = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationsList)
		assertNodeVisible(EvaluationsUiTags.EvaluationsWeekStrip)
		assertNodeVisible(EvaluationsUiTags.EvaluationsAddFab)
	}

	@Test
	fun when_hasNoEvaluations_then_displaysEmptyMatch() = runTuIndiceUiTest {
		val state = evaluationsContentState(
			originalEvaluations = emptyList()
		)

		setTuIndiceTestContent {
			EvaluationsContentView(
				state = state,
				onAddEvaluationClick = {},
				onEvaluationClick = { _, _, _ -> },
				onEvaluationEdit = {},
				onEvaluationDelete = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationsContentContainer)
		assertNodeVisible(EvaluationsUiTags.EvaluationsWeekStrip)
		assertNodeVisible(BaseUiTags.EmptyViewContainer)
		assertNodeVisible(EvaluationsUiTags.EvaluationsAddFab)
		assertNodeHidden(EvaluationsUiTags.EvaluationsList)
	}
}
