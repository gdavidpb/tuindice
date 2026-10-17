package com.gdavidpb.tuindice.assistant.ui.component.pensum

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.component.RequirementsComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectRef
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalTestApi::class)
class RequirementsComponentViewUiTest {
	@Test
	fun when_aSubjectHasRequirements_then_eachFilledSectionListsItsSubjectsWithTheirStatus() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RequirementsComponentView(component = requirements(), onAction = {})
		}

		assertNodeVisible(AssistantUiTags.Pensum.requirements(COMPONENT_ID))
		onNodeWithText("ESTRUCTURAS DE DATOS").assertIsDisplayed()
		onNodeWithText("REQUISITOS").assertIsDisplayed()
		onNodeWithText("IS-101").assertIsDisplayed()
		onNodeWithText("Programación I").assertIsDisplayed()
		onNodeWithText("Aprobada").assertIsDisplayed()
		onNodeWithText("DESBLOQUEA").assertIsDisplayed()
		onNodeWithText("IS-301").assertIsDisplayed()
		onNodeWithText("Bloqueada").assertIsDisplayed()
		onNodeWithText("CORREQUISITOS").assertDoesNotExist()
		onAllNodesWithText("Ver materia").assertCountEquals(2)
	}

	@Test
	fun when_aSubjectLinkIsTapped_then_itOpensThatSubjectsDetail() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			RequirementsComponentView(component = requirements(), onAction = { actions.add(it) })
		}

		onAllNodesWithText("Ver materia").onFirst().performClick()

		val open = assertIs<AssistantChat.Action.OpenNavTarget>(actions.single())
		assertEquals(NavTarget.SUBJECTS_DETAIL, open.target)
		assertEquals(listOf(EntityRef(EntityKind.SUBJECT, "IS-101")), open.entityRefs)
	}

	private fun requirements(): RequirementsComponent =
		RequirementsComponent(
			id = COMPONENT_ID,
			fallbackText = FALLBACK_TEXT,
			subjectCode = "IS-201",
			subjectName = "Estructuras de Datos",
			prerequisites = listOf(SubjectRef("IS-101", "Programación I", status = "approved")),
			corequisites = emptyList(),
			unlocks = listOf(SubjectRef("IS-301", "Bases de Datos", status = "blocked"))
		)

	private companion object {
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Requisitos de prueba"
	}
}
