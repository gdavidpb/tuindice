package com.gdavidpb.tuindice.login.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.login.ui.LoginUiTags
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

		assertNodeVisible(LoginUiTags.RandomFlipperText)
		onNodeWithText("Cargando datos").assertIsDisplayed()
	}

	@Test
	fun when_singleMessageProvided_then_messageRemainsStableAfterAnimationTicks() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RandomFlipperText(items = listOf("Autenticando"))
		}

		advanceAnimationsBy(1_500)

		assertNodeVisible(LoginUiTags.RandomFlipperText)
		onNodeWithText("Autenticando").assertIsDisplayed()
	}
}
