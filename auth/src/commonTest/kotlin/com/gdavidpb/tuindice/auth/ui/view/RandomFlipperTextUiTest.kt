package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.testkit.ui.advanceAnimationsBy
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class RandomFlipperTextUiTest {
	@Test
	fun when_singleMessageProvided_then_displaysIt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RandomFlipperText(items = listOf("Cargando datos"))
		}

		assertNodeVisible(AuthUiTags.RandomFlipperText)
		onNodeWithText("Cargando datos").assertIsDisplayed()
	}

	@Test
	fun when_singleMessageProvided_then_messageRemainsStableAfterAnimationTicks() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RandomFlipperText(items = listOf("Autenticando"))
		}

		advanceAnimationsBy(1_500)

		assertNodeVisible(AuthUiTags.RandomFlipperText)
		onNodeWithText("Autenticando").assertIsDisplayed()
	}

	@Test
	fun when_animationsAreDisabled_then_theMessageNeverRotates() = runTuIndiceUiTest {
		val messages = listOf("Autenticando", "Cargando datos", "Casi listo")

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				RandomFlipperText(items = messages)
			}
		}

		assertNodeVisible(AuthUiTags.RandomFlipperText)
		val shown = shownMessage()
		assertTrue(shown in messages)

		advanceAnimationsBy(5_000)

		assertEquals(shown, shownMessage())
	}

	private fun ComposeUiTest.shownMessage(): String =
		onNodeWithTag(AuthUiTags.RandomFlipperText)
			.fetchSemanticsNode()
			.config[SemanticsProperties.Text]
			.joinToString(separator = "") { it.text }
}
