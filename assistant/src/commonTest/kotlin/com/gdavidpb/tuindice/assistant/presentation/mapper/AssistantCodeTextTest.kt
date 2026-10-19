package com.gdavidpb.tuindice.assistant.presentation.mapper

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

// The codes of the chat cards read as the words Registro and the sync use. A code this build does not know reads as
// nothing, so its card shows no chip for it.
class AssistantCodeTextTest {
	@Test
	fun when_anOutcomeIsKnown_then_itReadsAsRegistroSaysIt() {
		assertEquals(UiText.Resource(Res.string.assistant_card_a_outcome_approved), attemptOutcomeText("approved"))
		assertEquals(UiText.Resource(Res.string.assistant_card_a_outcome_failed), attemptOutcomeText("failed"))
		assertEquals(UiText.Resource(Res.string.assistant_card_a_outcome_retired), attemptOutcomeText("retired"))
		assertEquals(UiText.Resource(Res.string.assistant_card_a_outcome_pending), attemptOutcomeText("pending"))
	}

	@Test
	fun when_anOutcomeIsUnreportedUnknownOrMissing_then_itHasNoChip() {
		assertNull(attemptOutcomeText("unreported"))
		assertNull(attemptOutcomeText("first_try"))
		assertNull(attemptOutcomeText(null))
	}

	@Test
	fun when_aBadgeIsKnown_then_itReadsAsItsWord() {
		assertEquals(
			UiText.Resource(Res.string.assistant_card_a_badge_without_effect),
			attemptBadgeText("without_effect")
		)
		assertEquals(UiText.Resource(Res.string.assistant_card_a_badge_equivalence), attemptBadgeText("equivalence"))
	}

	@Test
	fun when_aBadgeIsNoneUnknownOrMissing_then_itHasNoChip() {
		assertNull(attemptBadgeText("none"))
		assertNull(attemptBadgeText("first_try"))
		assertNull(attemptBadgeText(null))
	}

	@Test
	fun when_aSyncSourceIsKnown_then_itReadsAsItsState() {
		assertEquals(UiText.Resource(Res.string.assistant_card_a_source_success), syncSourceText("success"))
		assertEquals(UiText.Resource(Res.string.assistant_card_a_source_unavailable), syncSourceText("unavailable"))
		assertEquals(UiText.Resource(Res.string.assistant_card_a_source_not_attempted), syncSourceText("not_attempted"))
		assertEquals(UiText.Resource(Res.string.assistant_card_a_source_not_enrolled), syncSourceText("not_enrolled"))
	}

	@Test
	fun when_aSyncSourceIsUnknown_then_itHasNoChip_andIsNoProblem() {
		assertNull(syncSourceText("unknown"))
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
