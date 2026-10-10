package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.record.ui.model.CreateTermSubjectCardAction
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class CreateTermSubjectActionButtonViewUiTest {
	@Test
	fun when_theActionIsAdd_then_theButtonIsReadAsAddingTheSubject_andReportsItsTap() = runTuIndiceUiTest {
		var clicks = 0

		setTuIndiceTestContent {
			CreateTermSubjectActionButton(
				action = CreateTermSubjectCardAction.Add,
				enabled = true,
				onClick = { clicks++ },
				testTag = ButtonTag
			)
		}

		onNodeWithTag(ButtonTag)
			.assertIsDisplayed()
			.assertIsEnabled()
			.assertContentDescriptionEquals("Agregar materia")
			.performClick()

		assertEquals(1, clicks)
	}

	@Test
	fun when_theActionIsRemove_then_theButtonIsReadAsRemovingTheSubject() = runTuIndiceUiTest {
		var clicks = 0

		setTuIndiceTestContent {
			CreateTermSubjectActionButton(
				action = CreateTermSubjectCardAction.Remove,
				enabled = true,
				onClick = { clicks++ },
				testTag = ButtonTag
			)
		}

		onNodeWithTag(ButtonTag)
			.assertContentDescriptionEquals("Quitar materia")
			.performClick()

		assertEquals(1, clicks)
	}

	@Test
	fun when_theButtonIsDisabled_then_tappingItReportsNothing() = runTuIndiceUiTest {
		var clicks = 0

		setTuIndiceTestContent {
			CreateTermSubjectActionButton(
				action = CreateTermSubjectCardAction.Add,
				enabled = false,
				onClick = { clicks++ },
				testTag = ButtonTag
			)
		}

		onNodeWithTag(ButtonTag)
			.assertIsNotEnabled()
			// It still says what it would do, for whoever reaches it with a screen reader.
			.assertContentDescriptionEquals("Agregar materia")
			.performClick()

		assertEquals(0, clicks)
	}

	@Test
	fun when_noTagIsGiven_then_theButtonIsStillReachedByWhatItSays() = runTuIndiceUiTest {
		var clicks = 0

		setTuIndiceTestContent {
			CreateTermSubjectActionButton(
				action = CreateTermSubjectCardAction.Remove,
				enabled = true,
				onClick = { clicks++ }
			)
		}

		onNode(hasContentDescription("Quitar materia"))
			.assertIsDisplayed()
			.performClick()

		assertEquals(1, clicks)
	}

	private companion object {
		const val ButtonTag = "subject_action_button"
	}
}
