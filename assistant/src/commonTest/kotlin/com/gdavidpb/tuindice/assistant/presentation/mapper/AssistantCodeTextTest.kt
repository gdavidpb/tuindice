package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.CardChipItem
import com.gdavidpb.tuindice.assistant.presentation.model.CardChipTone
import com.gdavidpb.tuindice.base.presentation.model.UiText
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_badge_equivalence
import tuindice.assistant.generated.resources.assistant_card_a_badge_without_effect
import tuindice.assistant.generated.resources.assistant_card_a_outcome_approved
import tuindice.assistant.generated.resources.assistant_card_a_outcome_failed
import tuindice.assistant.generated.resources.assistant_card_a_outcome_pending
import tuindice.assistant.generated.resources.assistant_card_a_outcome_retired
import tuindice.assistant.generated.resources.assistant_card_a_source_not_attempted
import tuindice.assistant.generated.resources.assistant_card_a_source_not_enrolled
import tuindice.assistant.generated.resources.assistant_card_a_source_success
import tuindice.assistant.generated.resources.assistant_card_a_source_unavailable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

// The codes of the chat cards read as the words Registro and the sync use, each in the tone of its state. A code this
// build does not know reads as nothing, so its card shows no chip for it.
class AssistantCodeTextTest {
	@Test
	fun when_anOutcomeIsKnown_then_itHasItsWordAndItsTone() {
		assertEquals(
			CardChipItem(UiText.Resource(Res.string.assistant_card_a_outcome_approved), CardChipTone.Approved),
			attemptOutcomeChip("approved")
		)
		assertEquals(
			CardChipItem(UiText.Resource(Res.string.assistant_card_a_outcome_failed), CardChipTone.Failed),
			attemptOutcomeChip("failed")
		)
		assertEquals(
			CardChipItem(UiText.Resource(Res.string.assistant_card_a_outcome_retired), CardChipTone.Muted),
			attemptOutcomeChip("retired")
		)
		assertEquals(
			CardChipItem(UiText.Resource(Res.string.assistant_card_a_outcome_pending), CardChipTone.Current),
			attemptOutcomeChip("pending")
		)
	}

	@Test
	fun when_anOutcomeIsUnreportedUnknownOrMissing_then_itHasNoChip() {
		assertNull(attemptOutcomeChip("unreported"))
		assertNull(attemptOutcomeChip("first_try"))
		assertNull(attemptOutcomeChip(null))
	}

	@Test
	fun when_aBadgeIsKnown_then_itHasItsWordAndItsTone() {
		assertEquals(
			CardChipItem(UiText.Resource(Res.string.assistant_card_a_badge_without_effect), CardChipTone.Neutral),
			attemptBadgeChip("without_effect")
		)
		assertEquals(
			CardChipItem(UiText.Resource(Res.string.assistant_card_a_badge_equivalence), CardChipTone.Approved),
			attemptBadgeChip("equivalence")
		)
	}

	@Test
	fun when_aBadgeIsNoneUnknownOrMissing_then_itHasNoChip() {
		assertNull(attemptBadgeChip("none"))
		assertNull(attemptBadgeChip("first_try"))
		assertNull(attemptBadgeChip(null))
	}

	@Test
	fun when_aSyncSourceIsKnown_then_itHasItsState_andOnlyASourceThatAnsweredIsInTheSuccessTone() {
		assertEquals(
			CardChipItem(UiText.Resource(Res.string.assistant_card_a_source_success), CardChipTone.Success),
			syncSourceChip("success")
		)
		assertEquals(
			CardChipItem(UiText.Resource(Res.string.assistant_card_a_source_unavailable), CardChipTone.Failed),
			syncSourceChip("unavailable")
		)
		assertEquals(
			CardChipItem(UiText.Resource(Res.string.assistant_card_a_source_not_attempted), CardChipTone.Neutral),
			syncSourceChip("not_attempted")
		)
		assertEquals(
			CardChipItem(UiText.Resource(Res.string.assistant_card_a_source_not_enrolled), CardChipTone.Neutral),
			syncSourceChip("not_enrolled")
		)
	}

	@Test
	fun when_aSyncSourceIsUnknown_then_itHasNoChip_andIsNoProblem() {
		assertNull(syncSourceChip("unknown"))
		assertFalse(isSyncProblem("unknown"))
	}

	@Test
	fun when_aSyncSourceIsUnavailable_then_itIsTheOnlyProblem() {
		assertTrue(isSyncProblem("unavailable"))
		assertFalse(isSyncProblem("success"))
		assertFalse(isSyncProblem("not_attempted"))
		assertFalse(isSyncProblem("not_enrolled"))
		assertFalse(isSyncProblem(null))
	}
}
