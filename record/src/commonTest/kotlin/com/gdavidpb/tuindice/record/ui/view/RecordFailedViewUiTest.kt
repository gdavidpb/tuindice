package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.record.presentation.model.RecordFailedArt
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class RecordFailedViewUiTest {
	@Test
	fun when_theArtIsError_then_theErrorIllustrationHeadsTheMessage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RecordFailedView(
				title = "No pudimos cargar tu historial",
				message = "Intenta de nuevo en unos segundos.",
				art = RecordFailedArt.Error,
				retryText = "Reintentar",
				onRetryClick = {}
			)
		}

		assertNodeVisible(BaseUiTags.ErrorStateAnimation)
		assertNodeHidden(BaseUiTags.EmptyStateAnimation)
		onNodeWithTag(BaseUiTags.ErrorViewTitle).assertTextEquals("No pudimos cargar tu historial")
		onNodeWithTag(BaseUiTags.ErrorViewMessage).assertTextEquals("Intenta de nuevo en unos segundos.")
	}

	@Test
	fun when_theArtIsNoRecord_then_theCalmIllustrationTakesTheErrorOnesPlace() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RecordFailedView(
				title = "Aún no tienes expediente en la universidad",
				message = "Aparecerá aquí cuando la universidad lo publique.",
				art = RecordFailedArt.NoRecord,
				retryText = "Reintentar",
				onRetryClick = {}
			)
		}

		// A university with no record for the account yet is not a failure of ours.
		assertNodeVisible(BaseUiTags.EmptyStateAnimation)
		assertNodeHidden(BaseUiTags.ErrorStateAnimation)
		onNodeWithTag(BaseUiTags.ErrorViewTitle).assertTextEquals("Aún no tienes expediente en la universidad")
		onNodeWithTag(BaseUiTags.ErrorViewMessage)
			.assertTextEquals("Aparecerá aquí cuando la universidad lo publique.")
	}

	@Test
	fun when_retryIsTapped_then_theCallbackRuns_whicheverTheArt() = runTuIndiceUiTest {
		var retryClicks = 0

		setTuIndiceTestContent {
			RecordFailedView(
				title = "Aún no tienes expediente en la universidad",
				message = "Aparecerá aquí cuando la universidad lo publique.",
				art = RecordFailedArt.NoRecord,
				retryText = "Reintentar",
				onRetryClick = { retryClicks++ }
			)
		}

		onNodeWithTag(BaseUiTags.ErrorViewRetryButton)
			.assertTextEquals("Reintentar")
			.performClick()

		assertEquals(1, retryClicks)
	}
}
