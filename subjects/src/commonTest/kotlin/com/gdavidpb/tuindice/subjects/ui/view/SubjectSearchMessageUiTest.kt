package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SubjectSearchMessageUiTest {
	@Test
	fun when_descriptionProvided_then_displaysTitleAboveDescription() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectSearchMessage(
				modifier = Modifier.testTag(MESSAGE_TAG),
				title = "No encontramos materias",
				description = "Prueba con otro código o nombre."
			)
		}

		val texts = onNodeWithTag(MESSAGE_TAG).onChildren()

		texts.assertCountEquals(2)
		texts[0].assertTextEquals("No encontramos materias")
		texts[1].assertTextEquals("Prueba con otro código o nombre.")
	}

	@Test
	fun when_descriptionIsNull_then_displaysOnlyTheTitle() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectSearchMessage(
				modifier = Modifier.testTag(MESSAGE_TAG),
				title = "2 resultados para \"ci\""
			)
		}

		val texts = onNodeWithTag(MESSAGE_TAG).onChildren()

		texts.assertCountEquals(1)
		texts[0].assertTextEquals("2 resultados para \"ci\"")
	}

	@Test
	fun when_descriptionIsBlank_then_hidesTheDescription() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectSearchMessage(
				modifier = Modifier.testTag(MESSAGE_TAG),
				title = "No pudimos buscar materias",
				description = "   "
			)
		}

		val texts = onNodeWithTag(MESSAGE_TAG).onChildren()

		texts.assertCountEquals(1)
		texts[0].assertTextEquals("No pudimos buscar materias")
	}
}

private const val MESSAGE_TAG = "subject_search_message"
