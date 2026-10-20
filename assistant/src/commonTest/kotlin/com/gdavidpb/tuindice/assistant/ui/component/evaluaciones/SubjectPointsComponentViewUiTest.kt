package com.gdavidpb.tuindice.assistant.ui.component.evaluaciones

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.mapper.toSubjectPointsItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.component.SubjectPointsComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectPointsRow
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalTestApi::class)
class SubjectPointsComponentViewUiTest {
	@Test
	fun when_subjectsHavePoints_then_eachRowReadsItsEarnedOverItsEvaluatedPoints() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectPointsComponentView(item = points().toSubjectPointsItem(), onAction = {})
		}

		assertNodeVisible(AssistantUiTags.Evaluations.points(COMPONENT_ID))
		onNodeWithText("PUNTOS POR MATERIA").assertIsDisplayed()
		onNodeWithText("IS-101").assertIsDisplayed()
		onNodeWithText("Programación I").assertIsDisplayed()
		onNodeWithText("12.50/60").assertIsDisplayed()
		onNodeWithText("8/10").assertIsDisplayed()
	}

	@Test
	fun when_theCardNamesOneSubject_then_theLinkOpensItsDetailWithItsRef() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			SubjectPointsComponentView(
				item = points(refs = listOf(SUBJECT_REF)).toSubjectPointsItem(),
				onAction = { actions.add(it) }
			)
		}

		onNodeWithText("Ver materia").performClick()

		val open = assertIs<AssistantChat.Action.OpenNavTarget>(actions.single())
		assertEquals(NavTarget.SUBJECTS_DETAIL, open.target)
		assertEquals(listOf(SUBJECT_REF), open.entityRefs)
	}

	private fun points(refs: List<EntityRef> = emptyList()): SubjectPointsComponent =
		SubjectPointsComponent(
			id = COMPONENT_ID,
			fallbackText = FALLBACK_TEXT,
			entityRefs = refs,
			rows = listOf(
				SubjectPointsRow("IS-101", "Programación I", earned = 12.5, evaluated = 60.0, total = 100.0),
				SubjectPointsRow("IS-102", "Matemática I", earned = 8.0, evaluated = 10.0, total = 20.0)
			)
		)

	private companion object {
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Puntos de prueba"
		val SUBJECT_REF = EntityRef(EntityKind.SUBJECT, "IS-101")
	}
}
