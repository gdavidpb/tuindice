package com.gdavidpb.tuindice.assistant.ui.model

import com.gdavidpb.tuindice.base.presentation.model.UiText
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_band_very_high
import tuindice.assistant.generated.resources.assistant_card_b_evaluation_pending
import tuindice.assistant.generated.resources.assistant_card_b_subject_status_approved
import tuindice.assistant.generated.resources.assistant_card_b_subject_status_blocked
import tuindice.assistant.generated.resources.assistant_card_b_subject_status_in_progress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// The academic cards write the codes the service sends as words, and a code this build does not know has no word.
class AcademicCodeTextTest {
	@Test
	fun when_aSubjectStatusIsKnown_then_itHasItsWord() {
		assertEquals(UiText.Resource(Res.string.assistant_card_b_subject_status_approved), subjectStatusText("approved"))
		assertEquals(UiText.Resource(Res.string.assistant_card_b_subject_status_in_progress), subjectStatusText("current"))
		assertEquals(UiText.Resource(Res.string.assistant_card_b_subject_status_blocked), subjectStatusText("blocked"))
	}

	@Test
	fun when_anEvaluationStateOrABandIsKnown_then_itHasItsWord() {
		assertEquals(UiText.Resource(Res.string.assistant_card_b_evaluation_pending), evaluationStateText("pending"))
		assertEquals(UiText.Resource(Res.string.assistant_card_b_band_very_high), bandText("very_high"))
	}

	@Test
	fun when_aCodeIsUnknownOrMissing_then_itHasNoWord() {
		assertNull(subjectStatusText("sin_codigo"))
		assertNull(evaluationStateText(null))
		assertNull(bandText("extreme"))
	}
}
