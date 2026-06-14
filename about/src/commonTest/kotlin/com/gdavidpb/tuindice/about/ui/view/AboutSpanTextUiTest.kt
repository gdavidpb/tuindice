package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AboutSpanTextUiTest {
	@Test
	fun when_spanTextHasTitleAndDescription_then_rendersBothParts() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AboutSpanText(
				text = "Koin\nDependency Injection ligera para Kotlin"
			)
		}

		assertNodeVisible(AboutUiTags.SpanText)
		onNodeWithText("Koin", substring = true).assertIsDisplayed()
		onNodeWithText("Dependency Injection ligera para Kotlin", substring = true).assertIsDisplayed()
	}

	@Test
	fun when_spanTextHasOnlyTitle_then_rendersWithoutDuplicatingContentLine() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AboutSpanText(
				text = "Solo titulo"
			)
		}

		assertNodeVisible(AboutUiTags.SpanText)
		onNodeWithText("Solo titulo").assertIsDisplayed()
		onNodeWithTag(AboutUiTags.SpanText).assertTextEquals("Solo titulo")
	}
}
