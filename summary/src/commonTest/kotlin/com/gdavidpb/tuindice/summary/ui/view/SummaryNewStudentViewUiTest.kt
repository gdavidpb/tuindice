package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeDisabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeEnabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SummaryNewStudentViewUiTest {
	@Test
	fun when_theUniversityHasNoRecordYet_then_theCalmArtHeadsTheCopyItWasGiven() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryNewStudentView(
				title = "Aún no tienes expediente",
				message = "Aparecerá aquí cuando la universidad lo publique.",
				isRetryEnabled = true,
				onRetryClick = {}
			)
		}

		assertNodeVisible(SummaryUiTags.NewStudentContainer)
		// Nothing of ours failed, so it is the empty-state art and not the error one.
		assertNodeVisible(BaseUiTags.EmptyStateAnimation)
		assertNodeHidden(BaseUiTags.ErrorStateAnimation)
		assertNodeHidden(BaseUiTags.ErrorViewContainer)
		onNodeWithTag(SummaryUiTags.NewStudentTitle).assertTextEquals("Aún no tienes expediente")
		onNodeWithTag(SummaryUiTags.NewStudentMessage)
			.assertTextEquals("Aparecerá aquí cuando la universidad lo publique.")
	}

	@Test
	fun when_retryIsEnabledAndTapped_then_theCallbackRuns() = runTuIndiceUiTest {
		var retryClicks = 0

		setTuIndiceTestContent {
			SummaryNewStudentView(
				title = "Aún no tienes expediente",
				message = "Aparecerá aquí cuando la universidad lo publique.",
				isRetryEnabled = true,
				onRetryClick = { retryClicks++ }
			)
		}

		assertNodeEnabled(SummaryUiTags.NewStudentRetryButton)
		onNodeWithTag(SummaryUiTags.NewStudentRetryButton)
			.assertTextEquals("Reintentar")
			.performClick()

		assertEquals(1, retryClicks)
	}

	@Test
	fun when_aSyncIsRunning_then_retryWaits_andATapDoesNothing() = runTuIndiceUiTest {
		var retryClicks = 0

		setTuIndiceTestContent {
			SummaryNewStudentView(
				title = "Aún no tienes expediente",
				message = "Aparecerá aquí cuando la universidad lo publique.",
				isRetryEnabled = false,
				onRetryClick = { retryClicks++ }
			)
		}

		assertNodeVisible(SummaryUiTags.NewStudentRetryButton)
		assertNodeDisabled(SummaryUiTags.NewStudentRetryButton)
		onNodeWithTag(SummaryUiTags.NewStudentRetryButton).performClick()

		assertEquals(0, retryClicks)
	}
}
