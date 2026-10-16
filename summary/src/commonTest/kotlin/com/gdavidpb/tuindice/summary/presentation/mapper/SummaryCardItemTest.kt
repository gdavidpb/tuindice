package com.gdavidpb.tuindice.summary.presentation.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.CheckCircle
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCardItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_evaluations_invite_action
import tuindice.summary.generated.resources.summary_evaluations_pending_title
import tuindice.summary.generated.resources.summary_week_idle_title
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SummaryCardItemTest {
	@Test
	fun when_theEvaluationsAreNotDownloadedYet_then_TheCardClaimsNothingAndOffersNoAction() {
		val card = evaluationsPendingCard()

		assertEquals(Icons.AutoMirrored.Outlined.Assignment, card.icon)
		assertEquals(UiText.Resource(Res.string.summary_evaluations_pending_title), card.titleText)
		assertNull(card.action)
	}

	@Test
	fun when_aTermHasNoEvaluationYet_then_TheCardOffersTheFormThatAddsTheFirstOne() {
		val card = addEvaluationCard()

		assertEquals(
			SummaryCardItem.Action(
				text = UiText.Resource(Res.string.summary_evaluations_invite_action),
				target = SummaryHubTarget.AddEvaluation
			),
			card.action
		)
	}

	@Test
	fun when_nothingIsPendingAndNoEvaluationIsNextNamed_then_TheCardSaysSoWithTheDoneIcon() {
		val card = nothingPendingCard(next = null)

		assertEquals(Icons.Outlined.CheckCircle, card.icon)
		assertEquals(UiText.Resource(Res.string.summary_week_idle_title), card.titleText)
	}
}
