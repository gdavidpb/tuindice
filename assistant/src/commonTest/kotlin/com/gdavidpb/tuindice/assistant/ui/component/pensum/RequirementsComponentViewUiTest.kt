package com.gdavidpb.tuindice.assistant.ui.component.pensum

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.mapper.toRequirementsItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.hasLinkLabelContaining
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
	fun when_aSubjectHasRequirements_then_eachSectionListsItsSubjectsWithTheirStatus() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RequirementsComponentView(item = requirements().toRequirementsItem(), onAction = {})
		}

		assertNodeVisible(AssistantUiTags.Pensum.requirements(COMPONENT_ID))
		onNodeWithText("ESTRUCTURAS DE DATOS").assertIsDisplayed()
		onNodeWithText("Requisitos").assertIsDisplayed()
		onNodeWithText("IS-101").assertIsDisplayed()
		onNodeWithText("Programación I").assertIsDisplayed()
		onNodeWithText("Aprobada").assertIsDisplayed()
		onNodeWithText("Desbloquea").assertIsDisplayed()
		onNodeWithText("IS-301").assertIsDisplayed()
		onNodeWithText("Bloqueada").assertIsDisplayed()
		onNodeWithText("Correquisitos").assertIsDisplayed()
		onNodeWithText("Sin correquisitos").assertIsDisplayed()
		onAllNodes(hasLinkLabelContaining("Ver materia")).assertCountEquals(2)
	}

	@Test
	fun when_aSubjectRowIsTapped_then_itOpensThatSubjectsDetail() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			RequirementsComponentView(item = requirements().toRequirementsItem(), onAction = { actions.add(it) })
		}

		onAllNodes(hasLinkLabelContaining("Ver materia")).onFirst().performClick()

		val open = assertIs<AssistantChat.Action.OpenNavTarget>(actions.single())
		assertEquals(NavTarget.SUBJECTS_DETAIL, open.target)
		assertEquals(listOf(EntityRef(EntityKind.SUBJECT, "IS-101")), open.entityRefs)
	}

	@Test
	fun when_everySectionIsEmpty_then_eachKeepsItsLabelAndItsLine() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RequirementsComponentView(
				item = RequirementsComponent(
					id = COMPONENT_ID,
					fallbackText = FALLBACK_TEXT,
					subjectCode = "IS-201",
					subjectName = "Estructuras de Datos",
					prerequisites = emptyList(),
					corequisites = emptyList(),
					unlocks = emptyList()
				).toRequirementsItem(),
				onAction = {}
			)
		}

		onNodeWithText("Requisitos").assertIsDisplayed()
		onNodeWithText("Sin requisitos").assertIsDisplayed()
		onNodeWithText("Correquisitos").assertIsDisplayed()
		onNodeWithText("Sin correquisitos").assertIsDisplayed()
		onNodeWithText("Desbloquea").assertIsDisplayed()
		onNodeWithText("No desbloquea ninguna materia").assertIsDisplayed()
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
