package com.gdavidpb.tuindice.assistant.ui.component.materias

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.mapper.toSubjectListItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.hasLinkLabelContaining
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.component.SubjectListComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectRow
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalTestApi::class)
class SubjectListComponentViewUiTest {
	@Test
	fun when_subjectsAreListed_then_eachRowShowsItsCreditsStatusAndLink() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectListComponentView(item = list().toSubjectListItem(), rowLimit = null, onAction = {})
		}

		assertNodeVisible(AssistantUiTags.Subjects.list(COMPONENT_ID))
		onNodeWithText("IS-101").assertIsDisplayed()
		onNodeWithText("Programación I · 6 UC").assertIsDisplayed()
		onNodeWithText("Aprobada").assertIsDisplayed()
		onNodeWithText("IS-102").assertIsDisplayed()
		onNodeWithText("Matemática I · 8 UC").assertIsDisplayed()
		onAllNodes(hasLinkLabelContaining("Ver materia")).assertCountEquals(2)
	}

	@Test
	fun when_aSubjectRowIsTapped_then_itOpensThatSubjectsDetailWithItsCode() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			SubjectListComponentView(item = list().toSubjectListItem(), rowLimit = null, onAction = { actions.add(it) })
		}

		onAllNodes(hasLinkLabelContaining("Ver materia")).onFirst().performClick()

		val open = assertIs<AssistantChat.Action.OpenNavTarget>(actions.single())
		assertEquals(NavTarget.SUBJECTS_DETAIL, open.target)
		assertEquals(listOf(EntityRef(EntityKind.SUBJECT, "IS-101")), open.entityRefs)
	}

	@Test
	fun when_theListHasMoreRowsThanTheLimit_then_onlyTheLimitIsDrawn() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectListComponentView(item = list().toSubjectListItem(), rowLimit = 1, onAction = {})
		}

		onNodeWithText("IS-101").assertIsDisplayed()
		onNodeWithText("IS-102").assertDoesNotExist()
		onAllNodes(hasLinkLabelContaining("Ver materia")).assertCountEquals(1)
	}

	@Test
	fun when_noSubjectIsListed_then_aLineSaysSo() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectListComponentView(
				item = SubjectListComponent(id = COMPONENT_ID, fallbackText = FALLBACK_TEXT, items = emptyList())
					.toSubjectListItem(),
				rowLimit = null,
				onAction = {}
			)
		}

		onNodeWithText("No tienes materias que mostrar").assertIsDisplayed()
	}

	private fun list(): SubjectListComponent =
		SubjectListComponent(
			id = COMPONENT_ID,
			fallbackText = FALLBACK_TEXT,
			items = listOf(
				SubjectRow("IS-101", "Programación I", credits = 6, status = "approved"),
				SubjectRow("IS-102", "Matemática I", credits = 8)
			)
		)

	private companion object {
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Materias de prueba"
	}
}
