package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AboutHeaderUiTest {
	@Test
	fun when_headerRendered_then_displaysTitleAndSlotContent() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AboutHeader(text = "Dependencias") {
				Text("Contenido slot")
			}
		}

		assertNodeVisible(AboutUiTags.HeaderTitle)
		onNodeWithText("Dependencias").assertIsDisplayed()
		onNodeWithText("Contenido slot").assertIsDisplayed()
	}

	@Test
	fun when_titleIsEmpty_then_slotContentStillRenders() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AboutHeader(text = "") {
				Text("Solo slot")
			}
		}

		assertNodeVisible(AboutUiTags.HeaderTitle)
		onNodeWithText("Solo slot").assertIsDisplayed()
	}
}
