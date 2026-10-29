package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class TuIndiceSpanTextUiTest {
	@Test
	fun when_spanTextHasTitleAndDescription_then_rendersBothParts() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TuIndiceSpanText(
				text = "Koin\nDependency Injection ligera para Kotlin"
			)
		}

		assertNodeVisible(BaseUiTags.SpanText)
		onNodeWithText("Koin", substring = true).assertIsDisplayed()
		onNodeWithText("Dependency Injection ligera para Kotlin", substring = true).assertIsDisplayed()
	}

	@Test
	fun when_spanTextHasOnlyTitle_then_rendersWithoutDuplicatingContentLine() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TuIndiceSpanText(
				text = "Solo titulo"
			)
		}

		assertNodeVisible(BaseUiTags.SpanText)
		onNodeWithText("Solo titulo").assertIsDisplayed()
		onNodeWithTag(BaseUiTags.SpanText).assertTextEquals("Solo titulo")
	}
}
