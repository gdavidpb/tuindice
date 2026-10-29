package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.AssistantIntentOutcome
import com.gdavidpb.tuindice.assistant.presentation.model.IntentCardStatus
import com.gdavidpb.tuindice.assistant.presentation.model.IntentDraftItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.INTENT_ID
import com.gdavidpb.tuindice.assistant.ui.intentCard
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.testkit.ui.assertNodeDisabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeEnabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_intent_receipt_added_many
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

@OptIn(ExperimentalTestApi::class)
class AssistantIntentCardViewUiTest {
	@Test
	fun when_intentIsOpen_then_offersAtMostTwoButtonsAndConfirmDispatches() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantIntentCardView(card = intentCard(), onAction = { actions.add(it) })
		}

		assertNodeEnabled(AssistantUiTags.Intent.confirm(INTENT_ID))
		onAllNodes(
			hasClickAction() and hasAnyAncestor(hasTestTag(AssistantUiTags.Intent.card(INTENT_ID))),
			useUnmergedTree = true
		).assertCountEquals(2)
		onNodeWithTag(AssistantUiTags.Intent.confirm(INTENT_ID)).performClick()

		assertEquals(INTENT_ID, assertIs<AssistantChat.Action.ConfirmIntent>(actions.single()).intentId)
	}

	@Test
	fun when_intentIsDestructive_then_theConfirmStillDispatchesConfirm() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantIntentCardView(card = intentCard(isDestructive = true), onAction = { actions.add(it) })
		}

		assertNodeVisible(AssistantUiTags.Intent.confirm(INTENT_ID))
		onNodeWithTag(AssistantUiTags.Intent.confirm(INTENT_ID)).performClick()

		assertIs<AssistantChat.Action.ConfirmIntent>(actions.single())
	}

	@Test
	fun when_intentRuns_then_itsButtonsAreDisabledAndProgressShows() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantIntentCardView(card = intentCard(status = IntentCardStatus.Running), onAction = {})
		}

		assertNodeVisible(AssistantUiTags.Intent.progress(INTENT_ID))
		assertNodeDisabled(AssistantUiTags.Intent.confirm(INTENT_ID))
		assertNodeDisabled(AssistantUiTags.Intent.cancel(INTENT_ID))
	}

	@Test
	fun when_intentIsResolved_then_itBecomesItsReceiptWithoutButtons() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantIntentCardView(
				card = intentCard(status = IntentCardStatus.Resolved(AssistantIntentOutcome.CONFIRMED)),
				onAction = {}
			)
		}

		assertNodeVisible(AssistantUiTags.Intent.receipt(INTENT_ID))
		onNodeWithText(RECEIPT_TEXT).assertIsDisplayed()
		assertNodeHidden(AssistantUiTags.Intent.confirm(INTENT_ID))
		assertNodeHidden(AssistantUiTags.Intent.cancel(INTENT_ID))
	}

	@Test
	fun when_intentIsResolvedWithAReceipt_then_theReceiptSaysWhatWasAddedInsteadOfTheOutcome() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantIntentCardView(
				card = intentCard(
					status = IntentCardStatus.Resolved(
						outcome = AssistantIntentOutcome.CONFIRMED,
						receipt = UiText.Resource(Res.string.assistant_intent_receipt_added_many, listOf(2))
					)
				),
				onAction = {}
			)
		}

		assertNodeVisible(AssistantUiTags.Intent.receipt(INTENT_ID))
		onNodeWithText(ADDED_TEXT).assertIsDisplayed()
		onAllNodesWithText(RECEIPT_TEXT).assertCountEquals(0)
	}

	@Test
	fun when_draftIsTapped_then_dispatchesToggleIntentDraftWithTheNewValue() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		val draft = IntentDraftItem(
			mutationId = MUTATION_ID,
			label = "CI4325",
			detail = UiText.Raw("Parcial 1"),
			selected = true
		)
		setTuIndiceTestContent {
			AssistantIntentCardView(card = intentCard(drafts = listOf(draft)), onAction = { actions.add(it) })
		}

		assertNodeVisible(AssistantUiTags.Intent.draft(INTENT_ID, MUTATION_ID))
		onNodeWithTag(AssistantUiTags.Intent.draft(INTENT_ID, MUTATION_ID)).performClick()

		val toggle = assertIs<AssistantChat.Action.ToggleIntentDraft>(actions.single())
		assertEquals(MUTATION_ID, toggle.mutationId)
		assertFalse(toggle.selected)
	}

	private companion object {
		const val MUTATION_ID = "mutation-1"
		const val RECEIPT_TEXT = "Listo"
		const val ADDED_TEXT = "Se agregaron 2 evaluaciones"
	}
}
