package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubjectAvailability
import com.gdavidpb.tuindice.record.testing.createTermSubjectItem
import com.gdavidpb.tuindice.record.testing.textLayout
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class CreateTermSuggestedSubjectCardViewUiTest {
	@Test
	fun when_aSubjectIsSuggested_then_theCardDescribesIt_andAddReportsItsTap() = runTuIndiceUiTest {
		var addClicks = 0

		setTuIndiceTestContent {
			CreateTermSuggestedSubjectCard(
				subject = createTermSubjectItem(subjectCode = "FS1111", name = "Física I", credits = 3),
				enabled = true,
				onClick = { addClicks++ }
			)
		}

		assertNodeVisible(RecordUiTags.createSyntheticTermSubject("FS1111"))
		onNodeWithText("FS1111").assertIsDisplayed()
		onNodeWithText("FÍSICA I").assertIsDisplayed()
		onNodeWithText("3 UC").assertIsDisplayed()
		onNodeWithText("Disponible").assertIsDisplayed()

		// Read top to bottom: what it is, what it weighs, whether it can be taken.
		val name = onNodeWithText("FÍSICA I").getUnclippedBoundsInRoot()
		val credits = onNodeWithText("3 UC").getUnclippedBoundsInRoot()
		val status = onNodeWithText("Disponible").getUnclippedBoundsInRoot()

		assertTrue(name.bottom <= credits.top && credits.bottom <= status.top, "name, credits, then status")

		onNodeWithTag(addTag("FS1111"))
			.assertIsEnabled()
			.assertContentDescriptionEquals("Agregar materia")
			.performClick()

		assertEquals(1, addClicks)
	}

	@Test
	fun when_theSuggestionCannotBeAdded_then_itsAddButtonIsDisabled_andTheStatusSaysWhy() = runTuIndiceUiTest {
		var addClicks = 0

		setTuIndiceTestContent {
			CreateTermSuggestedSubjectCard(
				subject = createTermSubjectItem(availability = SyntheticTermSubjectAvailability.ALREADY_PLANNED),
				enabled = false,
				onClick = { addClicks++ }
			)
		}

		onNodeWithText("Ya planificada").assertIsDisplayed()
		// In the carousel the button keeps its place, so every card has the same shape.
		onNodeWithTag(addTag("MA1111"))
			.assertIsDisplayed()
			.assertIsNotEnabled()
			.performClick()

		assertEquals(0, addClicks)
	}

	@Test
	fun when_theNameIsLongerThanTheCard_then_itTakesTwoLinesAtMost() = runTuIndiceUiTest {
		val item = createTermSubjectItem(
			name = "Laboratorio de fenómenos de transporte y operaciones unitarias de ingeniería química"
		)

		setTuIndiceTestContent {
			CreateTermSuggestedSubjectCard(subject = item, enabled = true, onClick = {})
		}

		// The card has a fixed size: a long name is cut rather than pushing the status out of it.
		assertEquals(2, onNodeWithText(item.nameText).textLayout().lineCount)
		onNodeWithText("Disponible").assertIsDisplayed()
	}

	private fun addTag(subjectCode: String) = RecordUiTags.createSyntheticTermSubjectAction(
		subjectCode = subjectCode,
		action = "add"
	)
}
