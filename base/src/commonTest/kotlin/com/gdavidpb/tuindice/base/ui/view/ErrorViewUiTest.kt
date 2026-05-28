package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ErrorViewUiTest {
	@Test
	fun when_retryButtonIsTapped_then_invokesRetryCallback() = runTuIndiceUiTest {
		var retryCount = 0

		setTuIndiceTestContent {
			ErrorView(
				title = "Error de red",
				message = "No fue posible cargar los datos",
				retryText = "Reintentar",
				onRetryClick = { retryCount++ }
			)
		}

		assertNodeVisible(BaseUiTags.ErrorViewContainer)
		assertNodeVisible(BaseUiTags.ErrorViewTitle)
		assertNodeVisible(BaseUiTags.ErrorViewMessage)
		assertNodeVisible(BaseUiTags.ErrorViewRetryButton)

		onNodeWithTag(BaseUiTags.ErrorViewRetryButton).performClick()
		assertEquals(1, retryCount)
	}

	@Test
	fun when_headerContentProvided_then_rendersHeaderSlot() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ErrorView(
				title = "Error de red",
				message = "No fue posible cargar los datos",
				retryText = "Reintentar",
				onRetryClick = {},
				headerContent = {
					Text("Header slot")
				}
			)
		}

		onNodeWithText("Header slot").assertIsDisplayed()
	}
}
