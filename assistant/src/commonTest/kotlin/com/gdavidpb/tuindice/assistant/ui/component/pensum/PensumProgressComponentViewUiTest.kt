package com.gdavidpb.tuindice.assistant.ui.component.pensum

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.component.PensumProgressComponent
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalTestApi::class)
class PensumProgressComponentViewUiTest {
	@Test
	fun when_creditsAreApproved_then_theShareIsTheFigure_andTheStatusesCountTheSubjects() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumProgressComponentView(component = progress(), onAction = {})
		}

		assertNodeVisible(AssistantUiTags.Pensum.progress(COMPONENT_ID))
		onNodeWithText("AVANCE DEL PENSUM").assertIsDisplayed()
		onNodeWithContentDescription("Avance del pensum: 47 %").assertIsDisplayed()
		onNodeWithContentDescription("Créditos aprobados: 70 de 150").assertIsDisplayed()
		onNodeWithText("Aprobadas").assertIsDisplayed()
		onNodeWithText("12").assertIsDisplayed()
		onNodeWithText("En curso").assertIsDisplayed()
		onNodeWithText("3").assertIsDisplayed()
		onNodeWithText("Disponibles").assertIsDisplayed()
		onNodeWithText("5").assertIsDisplayed()
		onNodeWithText("Bloqueadas").assertIsDisplayed()
		onNodeWithText("8").assertIsDisplayed()
	}

	@Test
	fun when_theCardNamesThePensum_then_theLinkOpensThePensumWithItsRef() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			PensumProgressComponentView(
				component = progress(refs = listOf(PENSUM_REF)),
				onAction = { actions.add(it) }
			)
		}

		onNodeWithText("Ver pensum").performClick()

		val open = assertIs<AssistantChat.Action.OpenNavTarget>(actions.single())
		assertEquals(NavTarget.PENSUM, open.target)
		assertEquals(listOf(PENSUM_REF), open.entityRefs)
	}

	private fun progress(refs: List<EntityRef> = emptyList()): PensumProgressComponent =
		PensumProgressComponent(
			id = COMPONENT_ID,
			fallbackText = FALLBACK_TEXT,
			entityRefs = refs,
			approvedCredits = 70,
			totalCredits = 150,
			approved = 12,
			inProgress = 3,
			available = 5,
			blocked = 8
		)

	private companion object {
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Avance de prueba"
		val PENSUM_REF = EntityRef(EntityKind.PENSUM, "IS-2016")
	}
}
