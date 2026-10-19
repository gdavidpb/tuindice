package com.gdavidpb.tuindice.assistant.ui.component.pensum

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.mapper.toSubjectImpactItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.component.SubjectImpactComponent
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
class SubjectImpactComponentViewUiTest {
	@Test
	fun when_aSubjectDelaysTerms_then_theTermsAreTheFigure_andTheChainReadsInOrder() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectImpactComponentView(item = impact().toSubjectImpactItem(), onAction = {})
		}

		assertNodeVisible(AssistantUiTags.Pensum.impact(COMPONENT_ID))
		onNodeWithText("PROGRAMACIÓN I").assertIsDisplayed()
		onNodeWithContentDescription("Trimestres que retrasa: 2").assertIsDisplayed()
		onNodeWithText("IS-201").assertIsDisplayed()
		onNodeWithText("Estructuras de Datos").assertIsDisplayed()
		onNodeWithText("Disponible").assertIsDisplayed()
		onNodeWithText("IS-301").assertIsDisplayed()
		onNodeWithText("Bases de Datos").assertIsDisplayed()
	}

	@Test
	fun when_theCardNamesTheSubject_then_theLinkOpensItsDetail() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			SubjectImpactComponentView(item = impact(refs = listOf(SUBJECT_REF)).toSubjectImpactItem(), onAction = {
			    actions.add(it)
			})
		}

		onNodeWithText("Ver materia").performClick()

		val open = assertIs<AssistantChat.Action.OpenNavTarget>(actions.single())
		assertEquals(NavTarget.SUBJECTS_DETAIL, open.target)
		assertEquals(listOf(SUBJECT_REF), open.entityRefs)
	}

	private fun impact(refs: List<EntityRef> = emptyList()): SubjectImpactComponent =
		SubjectImpactComponent(
			id = COMPONENT_ID,
			fallbackText = FALLBACK_TEXT,
			entityRefs = refs,
			subjectCode = "IS-101",
			subjectName = "Programación I",
			chain = listOf(
				SubjectRef("IS-201", "Estructuras de Datos", status = "available"),
				SubjectRef("IS-301", "Bases de Datos")
			),
			lengthInTerms = 2
		)

	private companion object {
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Impacto de prueba"
		val SUBJECT_REF = EntityRef(EntityKind.SUBJECT, "IS-101")
	}
}
