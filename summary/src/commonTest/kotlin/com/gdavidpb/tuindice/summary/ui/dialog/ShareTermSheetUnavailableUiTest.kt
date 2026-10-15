package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ShareTermSheetUnavailableUiTest {
	@Test
	fun when_itIsShown_then_itSaysWhyThereIsNothingToShare() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareTermSheetUnavailable(title = "Sin estadísticas", message = "Vuelve a intentarlo más tarde.")
		}

		onNodeWithTag(SummaryUiTags.ShareTermUnavailableTitle).assertTextEquals("Sin estadísticas")
		onNodeWithTag(SummaryUiTags.ShareTermUnavailableMessage).assertTextEquals("Vuelve a intentarlo más tarde.")
	}

	@Test
	fun when_itIsShown_then_theSheetIsTaggedAsUnavailable() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareTermSheetUnavailable(title = "Sin estadísticas", message = "Vuelve a intentarlo más tarde.")
		}

		onNodeWithTag(SummaryUiTags.ShareTermUnavailable).assertIsDisplayed()
	}
}
