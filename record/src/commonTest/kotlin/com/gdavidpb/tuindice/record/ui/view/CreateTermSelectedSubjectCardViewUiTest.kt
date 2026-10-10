package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubjectAvailability
import com.gdavidpb.tuindice.record.testing.createTermSubjectItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.CreateTermSubjectCardAction
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class CreateTermSelectedSubjectCardViewUiTest {
	@Test
	fun when_theSubjectIsInTheTerm_then_theCardDescribesIt_andOffersToRemoveIt() = runTuIndiceUiTest {
		var removeClicks = 0

		setTuIndiceTestContent {
			CreateTermSelectedSubjectCard(
				subject = createTermSubjectItem(subjectCode = "MA1111", name = "Matemáticas I", credits = 4),
				action = CreateTermSubjectCardAction.Remove,
				onClick = { removeClicks++ }
			)
		}

		assertNodeVisible(RecordUiTags.createSyntheticTermSubject("MA1111"))
		onNodeWithText("MA1111").assertIsDisplayed()
		onNodeWithText("MATEMÁTICAS I").assertIsDisplayed()
		onNodeWithText("4 UC").assertIsDisplayed()
		onNodeWithText("Disponible").assertIsDisplayed()

		onNodeWithTag(actionTag(action = "remove"))
			.assertContentDescriptionEquals("Quitar materia")
			.performClick()

		assertEquals(1, removeClicks)
		// Nobody asked for the stats, so the card offers no way to them.
		assertNodeHidden(RecordUiTags.createSyntheticTermSubjectStatsButton("MA1111"))
		assertNodeHidden(actionTag(action = "add"))
	}

	@Test
	fun when_theSubjectCanBeAdded_then_theCardOffersToAddIt() = runTuIndiceUiTest {
		var addClicks = 0

		setTuIndiceTestContent {
			CreateTermSelectedSubjectCard(
				subject = createTermSubjectItem(),
				action = CreateTermSubjectCardAction.Add,
				enabled = true,
				onClick = { addClicks++ }
			)
		}

		onNodeWithTag(actionTag(action = "add"))
			.assertIsEnabled()
			.assertContentDescriptionEquals("Agregar materia")
			.performClick()

		assertEquals(1, addClicks)
		assertNodeHidden(actionTag(action = "remove"))
	}

	@Test
	fun when_theSubjectCannotBeAdded_then_theCardShowsWhy_andNoAddButton() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateTermSelectedSubjectCard(
				subject = createTermSubjectItem(availability = SyntheticTermSubjectAvailability.APPROVED),
				action = CreateTermSubjectCardAction.Add,
				enabled = false,
				onClick = {}
			)
		}

		onNodeWithText("Aprobada").assertIsDisplayed()
		// A button that can do nothing is not drawn at all: the status already says why.
		assertNodeHidden(actionTag(action = "add"))
		assertNodeHidden(RecordUiTags.createSyntheticTermSubjectStatsButton("MA1111"))
	}

	@Test
	fun when_removingIsDisabled_then_theRemoveButtonStays_butCannotBeUsed() = runTuIndiceUiTest {
		var removeClicks = 0

		setTuIndiceTestContent {
			CreateTermSelectedSubjectCard(
				subject = createTermSubjectItem(),
				action = CreateTermSubjectCardAction.Remove,
				enabled = false,
				onClick = { removeClicks++ }
			)
		}

		onNodeWithTag(actionTag(action = "remove"))
			.assertIsDisplayed()
			.assertIsNotEnabled()
			.performClick()

		assertEquals(0, removeClicks)
	}

	@Test
	fun when_theStatsCanBeOpened_then_theirButtonSitsUnderTheAction_andReportsTheSubject() = runTuIndiceUiTest {
		val openedStats = mutableListOf<String>()

		setTuIndiceTestContent {
			CreateTermSelectedSubjectCard(
				subject = createTermSubjectItem(availability = SyntheticTermSubjectAvailability.APPROVED),
				action = CreateTermSubjectCardAction.Add,
				enabled = false,
				onClick = {},
				onStatsClick = { subjectCode -> openedStats += subjectCode }
			)
		}

		// A subject that cannot be added still has stats worth reading.
		assertNodeHidden(actionTag(action = "add"))
		onNodeWithTag(RecordUiTags.createSyntheticTermSubjectStatsButton("MA1111"))
			.assertContentDescriptionEquals("Ver estadísticas de MA1111")
			.performClick()

		assertEquals(listOf("MA1111"), openedStats)
	}

	private fun actionTag(action: String) = RecordUiTags.createSyntheticTermSubjectAction(
		subjectCode = "MA1111",
		action = action
	)
}
