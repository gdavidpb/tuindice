package com.gdavidpb.tuindice.evaluations.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.testing.evaluationContentState
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class EvaluationScreenUiTest {
	@Test
	fun when_stateIsLoading_then_displaysLoadingView() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationScreen(
				state = Evaluation.State.Loading,
				onAttemptChange = {},
				onTypeChange = {},
				onDateChange = {},
				onGradeClick = { _, _, _, _ -> },
				onMaxGradeClick = { _, _, _ -> },
				onDoneClick = { _, _, _, _, _, _ -> },
				onRetryClick = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationLoadingIndicator)
	}

	@Test
	fun when_stateIsContent_then_displaysContentView() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationScreen(
				state = evaluationContentState(),
				onAttemptChange = {},
				onTypeChange = {},
				onDateChange = {},
				onGradeClick = { _, _, _, _ -> },
				onMaxGradeClick = { _, _, _ -> },
				onDoneClick = { _, _, _, _, _, _ -> },
				onRetryClick = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationContentContainer)
	}

	@Test
	fun when_stateIsFailed_then_retryDispatchesCallback() = runTuIndiceUiTest {
		var retryClicks = 0

		setTuIndiceTestContent {
			EvaluationScreen(
				state = Evaluation.State.Failed,
				onAttemptChange = {},
				onTypeChange = {},
				onDateChange = {},
				onGradeClick = { _, _, _, _ -> },
				onMaxGradeClick = { _, _, _ -> },
				onDoneClick = { _, _, _, _, _, _ -> },
				onRetryClick = { retryClicks++ }
			)
		}

		assertNodeVisible(BaseUiTags.ErrorViewRetryButton)
		onNodeWithTag(BaseUiTags.ErrorViewRetryButton).performClick()
		assertEquals(1, retryClicks)
	}
}
