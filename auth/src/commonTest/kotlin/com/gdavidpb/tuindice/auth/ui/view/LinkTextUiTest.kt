package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class LinkTextUiTest {
	@Test
	fun when_linkTextRendered_then_displaysCompleteMessage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			LinkText(
				text = "He leido los terminos y condiciones",
				style = TextStyle.Default,
				linkStyle = SpanStyle(color = Color.Blue),
				links = mapOf(
					"terminos" to {},
					"condiciones" to {}
				)
			)
		}

		onNodeWithText("He leido los terminos y condiciones").assertIsDisplayed()
	}

	@Test
	fun when_singleLinkTextTapped_then_invokesLinkCallback() = runTuIndiceUiTest {
		var clicks = 0

		setTuIndiceTestContent {
			LinkText(
				text = "Terminos",
				style = TextStyle.Default,
				linkStyle = SpanStyle(color = Color.Blue),
				links = mapOf("Terminos" to { clicks++ })
			)
		}

		onNodeWithText("Terminos").performClick()
		assertEquals(1, clicks)
	}
}
