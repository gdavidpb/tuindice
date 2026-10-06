package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PensumLoadingViewUiTest {
	@Test
	fun when_loadingViewIsRendered_then_showsLoadingTitleAndMessage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumLoadingView()
		}

		assertNodeVisible(PensumUiTags.Loading)
		onNodeWithTag(PensumUiTags.LoadingTitle).assertTextEquals("Preparando tu pensum")
		onNodeWithTag(PensumUiTags.LoadingMessage)
			.assertTextEquals("Estamos armando la ruta de materias y prelaciones.")
	}

	@Test
	fun when_loadingViewIsRendered_then_hostsTheAnimationAndOffersNoAction() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumLoadingView()
		}

		onNode(
			hasTestTag(PensumUiTags.LoadingAnimation) and
				hasAnyAncestor(hasTestTag(PensumUiTags.Loading))
		).assertExists()
		onAllNodes(hasClickAction()).assertCountEquals(0)
	}
}
