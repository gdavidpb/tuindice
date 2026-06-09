package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SubjectResultCardUiTest {
	@Test
	fun when_optionalContentIsProvided_then_rendersStatusAndTrailingSlots() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectResultCard(
				subjectCode = "CI2511",
				nameText = "Lógica Simbólica",
				creditsText = "4 UC",
				containerTestTag = CardTag,
				statusContent = {
					Text(
						modifier = Modifier.testTag(StatusTag),
						text = "Disponible"
					)
				},
				trailingContent = {
					Text(
						modifier = Modifier.testTag(ActionTag),
						text = "Agregar"
					)
				}
			)
		}

		onNodeWithTag(CardTag).assertIsDisplayed()
		onNodeWithText("CI2511").assertIsDisplayed()
		onNodeWithText("Lógica Simbólica").assertIsDisplayed()
		onNodeWithText("4 UC").assertIsDisplayed()
		onNodeWithTag(StatusTag).assertIsDisplayed()
		onNodeWithTag(ActionTag).assertIsDisplayed()
	}

	@Test
	fun when_optionalContentIsMissing_then_rendersCoreSubjectInformationOnly() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectResultCard(
				subjectCode = "MA1111",
				nameText = "Matemáticas I",
				creditsText = "5 UC",
				containerTestTag = CardTag
			)
		}

		onNodeWithTag(CardTag).assertIsDisplayed()
		onNodeWithText("MA1111").assertIsDisplayed()
		assertNodeHidden(StatusTag)
		assertNodeHidden(ActionTag)
	}

	private companion object {
		const val CardTag = "subject_result_card"
		const val StatusTag = "subject_result_status"
		const val ActionTag = "subject_result_action"
	}
}
