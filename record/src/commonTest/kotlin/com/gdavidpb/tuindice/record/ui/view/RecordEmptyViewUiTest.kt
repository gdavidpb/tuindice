package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class RecordEmptyViewUiTest {
	@Test
	fun when_emptyViewIsRendered_then_displaysMessageAndIllustration() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RecordEmptyView(
				message = "TuIndice te da la bienvenida a La Simon",
				highlightedParts = listOf("TuIndice", "La Simon")
			)
		}

		assertNodeVisible(RecordUiTags.EmptyContainer)
		assertNodeVisible(RecordUiTags.EmptyIllustration)
		assertNodeVisible(RecordUiTags.EmptyMessage)
		onNodeWithText("TuIndice te da la bienvenida a La Simon").assertExists()
	}

	@Test
	fun when_highlightedPartsContainBlanksOrMissingTokens_then_rendersMessageNormally() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RecordEmptyView(
				message = "Sin información para mostrar",
				highlightedParts = listOf("", " ", "NoExiste")
			)
		}

		assertNodeVisible(RecordUiTags.EmptyContainer)
		onNodeWithText("Sin información para mostrar").assertExists()
	}
}
