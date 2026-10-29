package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AssistantProgressViewUiTest {
	@Test
	fun when_labelIsSet_then_showsTheAtomAndTheLabel() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantProgressView(label = LABEL)
		}

		assertNodeVisible(AssistantUiTags.ProgressAtom)
		onNodeWithText(LABEL).assertIsDisplayed()
	}

	@Test
	fun when_animationsAreOff_then_theAtomAndTheLabelStayStill() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				AssistantProgressView(label = LABEL)
			}
		}

		assertNodeVisible(AssistantUiTags.ProgressAtom)
		assertNodeVisible(AssistantUiTags.ProgressLabel)
		onNodeWithTag(AssistantUiTags.ProgressLabel).assertIsDisplayed()
	}

	private companion object {
		const val LABEL = "Revisando tu pensum"
	}
}
