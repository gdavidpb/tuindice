package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.testkit.ui.advanceAnimationsBy
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class TopAppBarAnimatedTitleViewUiTest {
	@Test
	fun when_titleChanges_then_rendersUpdatedTitle() = runTuIndiceUiTest {
		val titleState = mutableStateOf("Inicio")

		setTuIndiceTestContent {
			TopAppBarAnimatedTitleView(title = titleState.value)
		}

		onNodeWithTag(BaseUiTags.TopAppBarTitle).assertIsDisplayed()
		onNodeWithText("Inicio").assertIsDisplayed()

		runOnIdle {
			titleState.value = "Resumen"
		}
		advanceAnimationsBy(millis = 500)

		onNodeWithText("Resumen").assertIsDisplayed()
	}

	@Test
	fun when_viewRendered_then_displaysInitialTitle() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TopAppBarAnimatedTitleView(title = "Informe Academico")
		}

		onNodeWithTag(BaseUiTags.TopAppBarTitle).assertIsDisplayed()
		onNodeWithText("Informe Academico").assertIsDisplayed()
	}
}
