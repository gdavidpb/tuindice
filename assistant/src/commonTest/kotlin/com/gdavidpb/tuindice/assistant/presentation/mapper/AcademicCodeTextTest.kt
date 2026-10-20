package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.CardChipItem
import com.gdavidpb.tuindice.assistant.presentation.model.CardChipTone
import com.gdavidpb.tuindice.base.presentation.model.UiText
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_outcome_approved
import tuindice.assistant.generated.resources.assistant_card_a_outcome_pending
import tuindice.assistant.generated.resources.assistant_card_b_band_very_high
import tuindice.assistant.generated.resources.assistant_card_b_evaluation_continuous
import tuindice.assistant.generated.resources.assistant_card_b_evaluation_overdue
import tuindice.assistant.generated.resources.assistant_card_b_evaluation_pending
import tuindice.assistant.generated.resources.assistant_card_b_subject_status_available
import tuindice.assistant.generated.resources.assistant_card_b_subject_status_blocked
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// The academic cards write the codes the service sends as words, in the tone of their state, and a code this build does
// not know has no chip.
class AcademicCodeTextTest {
	@Test
	fun when_aSubjectStatusIsKnown_then_itHasItsWordAndItsTone() {
		assertEquals(
			CardChipItem(UiText.Resource(Res.string.assistant_card_a_outcome_approved), CardChipTone.Approved),
			subjectStatusChip("approved")
		)
		assertEquals(
			CardChipItem(UiText.Resource(Res.string.assistant_card_a_outcome_pending), CardChipTone.Current),
			subjectStatusChip("current")
		)
		assertEquals(
			CardChipItem(UiText.Resource(Res.string.assistant_card_b_subject_status_available), CardChipTone.Muted),
			subjectStatusChip("available")
		)
		assertEquals(
			CardChipItem(UiText.Resource(Res.string.assistant_card_b_subject_status_blocked), CardChipTone.Muted),
			subjectStatusChip("blocked")
		)
	}

	@Test
	fun when_anEvaluationStateOrABandIsKnown_then_itIsANeutralChip() {
		assertEquals(
			CardChipItem(UiText.Resource(Res.string.assistant_card_b_evaluation_pending), CardChipTone.Neutral),
			evaluationStateChip("pending")
		)
		assertEquals(
			CardChipItem(UiText.Resource(Res.string.assistant_card_b_evaluation_continuous), CardChipTone.Neutral),
			evaluationStateChip("continuous")
		)
		assertEquals(
			CardChipItem(UiText.Resource(Res.string.assistant_card_b_evaluation_overdue), CardChipTone.Failed),
			evaluationStateChip("overdue")
		)
		assertEquals(
			CardChipItem(UiText.Resource(Res.string.assistant_card_b_band_very_high), CardChipTone.Neutral),
			bandChip("very_high")
		)
	}

	@Test
	fun when_aCodeIsUnknownOrMissing_then_itHasNoChip() {
		assertNull(subjectStatusChip("sin_codigo"))
		assertNull(evaluationStateChip(null))
		assertNull(bandChip("extreme"))
		assertEquals("Exigente", planLoadBandText(" Exigente "))
		assertNull(planLoadBandText(" "))
	}
}
