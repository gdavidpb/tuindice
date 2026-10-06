package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SubjectSearchErrorUiTest {
	@Test
	fun when_rendered_then_displaysErrorTitleMessageAndRetryLabel() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectSearchError(onRetryClick = {})
		}

		onNodeWithText("No pudimos buscar materias").assertIsDisplayed()
		onNodeWithText("Intenta de nuevo.").assertIsDisplayed()
		onNodeWithTag(SubjectsUiTags.SearchRetry)
			.assertTextEquals("Reintentar")
			.assertIsEnabled()
	}

	@Test
	fun when_retryTapped_then_invokesRetryCallbackOncePerTap() = runTuIndiceUiTest {
		var retryClicks = 0

		setTuIndiceTestContent {
			SubjectSearchError(onRetryClick = { retryClicks++ })
		}

		onNodeWithTag(SubjectsUiTags.SearchRetry).performClick()
		assertEquals(1, retryClicks)

		onNodeWithTag(SubjectsUiTags.SearchRetry).performClick()
		assertEquals(2, retryClicks)
	}
}
