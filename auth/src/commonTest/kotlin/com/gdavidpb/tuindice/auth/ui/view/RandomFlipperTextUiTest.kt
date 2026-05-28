package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.testkit.ui.advanceAnimationsBy
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

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
}
