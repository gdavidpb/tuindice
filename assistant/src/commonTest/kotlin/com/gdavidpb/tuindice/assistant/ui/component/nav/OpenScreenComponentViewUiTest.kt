package com.gdavidpb.tuindice.assistant.ui.component.nav

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.component.OpenScreenComponent
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalTestApi::class)
class OpenScreenComponentViewUiTest {
	@Test
	fun when_labelIsShown_then_itsButtonOpensTheTargetScreen() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			OpenScreenComponentView(
				component = OpenScreenComponent(
					id = COMPONENT_ID,
					fallbackText = LABEL,
					target = NavTarget.EVALUATIONS,
					label = LABEL
				),
				onAction = { actions.add(it) }
			)
		}

		assertNodeVisible(AssistantUiTags.Component.openScreen(COMPONENT_ID))
		onNodeWithText(LABEL).assertIsDisplayed()
		onNodeWithTag(AssistantUiTags.Component.openScreen(COMPONENT_ID)).performClick()

		val open = assertIs<AssistantChat.Action.OpenNavTarget>(actions.single())
		assertEquals(NavTarget.EVALUATIONS, open.target)
	}

	@Test
	fun when_componentNamesRecords_then_itPassesThemToTheNavigation() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		val entityRefs = listOf(EntityRef(kind = EntityKind.SUBJECT, id = SUBJECT_ID))
		setTuIndiceTestContent {
			OpenScreenComponentView(
				component = OpenScreenComponent(
					id = COMPONENT_ID,
					fallbackText = LABEL,
					entityRefs = entityRefs,
					target = NavTarget.SUBJECTS_DETAIL,
					label = LABEL
				),
				onAction = { actions.add(it) }
			)
		}

		onNodeWithTag(AssistantUiTags.Component.openScreen(COMPONENT_ID)).performClick()

		val open = assertIs<AssistantChat.Action.OpenNavTarget>(actions.single())
		assertEquals(entityRefs, open.entityRefs)
	}

	private companion object {
		const val SUBJECT_ID = "subject-1"
		const val COMPONENT_ID = "component-1"
		const val LABEL = "Ver evaluaciones"
	}
}
