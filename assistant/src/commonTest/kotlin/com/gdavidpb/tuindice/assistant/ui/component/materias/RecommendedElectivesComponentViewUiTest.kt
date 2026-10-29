package com.gdavidpb.tuindice.assistant.ui.component.materias

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.mapper.toRecommendedElectivesItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.component.RecommendedElective
import com.gdavidpb.tuindice.chatcontract.component.RecommendedElectivesComponent
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalTestApi::class)
class RecommendedElectivesComponentViewUiTest {
	@Test
	fun when_electivesAreRecommended_then_eachReadsItsCodeNameAndReason() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RecommendedElectivesComponentView(item = electives().toRecommendedElectivesItem(), rowLimit = null, onAction = {})
		}

		assertNodeVisible(AssistantUiTags.Subjects.electives(COMPONENT_ID))
		onNodeWithText("ELECTIVAS RECOMENDADAS").assertIsDisplayed()
		onNodeWithText("IS-550").assertIsDisplayed()
		onNodeWithText("Inteligencia Artificial").assertIsDisplayed()
		onNodeWithText("Encaja con tu avance").assertIsDisplayed()
	}

	@Test
	fun when_theCardNamesOneSubject_then_theLinkOpensItsDetailWithItsRef() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			RecommendedElectivesComponentView(
				item = electives(refs = listOf(SUBJECT_REF)).toRecommendedElectivesItem(),
				rowLimit = null,
				onAction = { actions.add(it) }
			)
		}

		onNodeWithText("Ver materia").performClick()

		val open = assertIs<AssistantChat.Action.OpenNavTarget>(actions.single())
		assertEquals(NavTarget.SUBJECTS_DETAIL, open.target)
		assertEquals(listOf(SUBJECT_REF), open.entityRefs)
	}

	@Test
	fun when_noElectiveIsRecommended_then_aLineSaysSo() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RecommendedElectivesComponentView(
				item = RecommendedElectivesComponent(id = COMPONENT_ID, fallbackText = FALLBACK_TEXT, items = emptyList())
					.toRecommendedElectivesItem(),
				rowLimit = null,
				onAction = {}
			)
		}

		onNodeWithText("No hay electivas que recomendar ahora").assertIsDisplayed()
	}

	private fun electives(refs: List<EntityRef> = emptyList()): RecommendedElectivesComponent =
		RecommendedElectivesComponent(
			id = COMPONENT_ID,
			fallbackText = FALLBACK_TEXT,
			entityRefs = refs,
			items = listOf(RecommendedElective("IS-550", "Inteligencia Artificial", 4, "Encaja con tu avance"))
		)

	private companion object {
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Electivas de prueba"
		val SUBJECT_REF = EntityRef(EntityKind.SUBJECT, "IS-550")
	}
}
