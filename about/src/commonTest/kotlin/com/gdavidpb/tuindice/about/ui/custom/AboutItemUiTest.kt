package com.gdavidpb.tuindice.about.ui.custom

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class AboutItemUiTest {
	@Test
	fun when_itemTapped_then_invokesClickCallback() = runTuIndiceUiTest {
		var clickCount = 0

		setTuIndiceTestContent {
			AboutItem(
				icon = ColorPainter(Color.Red),
				text = "Titulo\nDescripcion",
				testTag = AboutUiTags.ItemContainer,
				onClick = { clickCount++ }
			)
		}

		assertNodeVisible(AboutUiTags.ItemContainer)
		onNodeWithTag(AboutUiTags.ItemContainer).performClick()
		assertEquals(1, clickCount)
	}

	@Test
	fun when_itemTextHasMultipleLines_then_iconContentDescriptionUsesTitleLine() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AboutItem(
				icon = ColorPainter(Color.Blue),
				text = "Titulo principal\nDescripcion secundaria"
			)
		}

		onNodeWithContentDescription("Titulo principal").assertIsDisplayed()
	}
}
