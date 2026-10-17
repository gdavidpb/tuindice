package com.gdavidpb.tuindice.assistant.ui.component.conocimiento

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.Citation
import com.gdavidpb.tuindice.chatcontract.component.CitationComponent
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class CitationComponentViewUiTest {
	@Test
	fun when_aSourceIsCited_then_itsDocumentArticleExcerptAndAddressRead() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CitationComponentView(component = citation(url = ADDRESS), onAction = {})
		}

		assertNodeVisible(AssistantUiTags.Knowledge.citation(COMPONENT_ID))
		onNodeWithText("FUENTES").assertIsDisplayed()
		onNodeWithText("Reglamento de evaluación ›").assertIsDisplayed()
		onNodeWithText("Artículo 12").assertIsDisplayed()
		onNodeWithText(EXCERPT).assertIsDisplayed()
		onNodeWithText(ADDRESS).assertIsDisplayed()
	}

	@Test
	fun when_aSourceHasNoAddress_then_noAddressIsDrawn() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CitationComponentView(component = citation(url = null), onAction = {})
		}

		onNodeWithText(EXCERPT).assertIsDisplayed()
		onNodeWithText("https://", substring = true).assertDoesNotExist()
	}

	private fun citation(url: String?): CitationComponent =
		CitationComponent(
			id = COMPONENT_ID,
			fallbackText = FALLBACK_TEXT,
			sources = listOf(
				Citation(
					document = "Reglamento de evaluación",
					article = "Artículo 12",
					excerpt = EXCERPT,
					url = url
				)
			)
		)

	private companion object {
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Fuentes de prueba"
		const val EXCERPT = "Las evaluaciones se califican sobre veinte puntos."
		const val ADDRESS = "https://example.com/reglamento"
	}
}
