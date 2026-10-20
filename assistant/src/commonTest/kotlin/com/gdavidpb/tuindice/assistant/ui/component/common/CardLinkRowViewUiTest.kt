package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.CardLinkItem
import com.gdavidpb.tuindice.assistant.ui.hasLinkLabel
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalTestApi::class)
class CardLinkRowViewUiTest {
	@Test
	fun when_aRowLinksToAScreen_then_theWholeRowOpensItAndItsVisibleLinkTextIsGone() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			CardLinkRowView(
				link = subjectLink(),
				onAction = { actions.add(it) },
				text = CardRowText(name = "Programación I", codes = listOf("IS-101"))
			)
		}
		waitForIdle()

		onNodeWithText("Programación I").assertIsDisplayed()
		onNodeWithText("Ver materia").assertDoesNotExist()
		onNode(hasLinkLabel("Ver materia IS-101")).performClick()

		val open = assertIs<AssistantChat.Action.OpenNavTarget>(actions.single())
		assertEquals(NavTarget.SUBJECTS_DETAIL, open.target)
		assertEquals(listOf(EntityRef(EntityKind.SUBJECT, "IS-101")), open.entityRefs)
	}

	@Test
	fun when_aRowHasNoLink_then_itIsNotATap() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CardLinkRowView(link = null, onAction = {}, text = CardRowText(name = "Miércoles · bloque 3"))
		}
		waitForIdle()

		onNodeWithText("Miércoles · bloque 3").assertHasNoClickAction()
	}

	private fun subjectLink(): CardLinkItem =
		CardLinkItem(
			target = NavTarget.SUBJECTS_DETAIL,
			text = UiText.Raw("Ver materia"),
			entityRefs = listOf(EntityRef(EntityKind.SUBJECT, "IS-101")),
			accessibilityText = UiText.Raw("Ver materia IS-101")
		)
}
