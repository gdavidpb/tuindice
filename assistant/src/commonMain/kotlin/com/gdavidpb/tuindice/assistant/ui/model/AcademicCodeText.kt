package com.gdavidpb.tuindice.assistant.ui.model

import com.gdavidpb.tuindice.base.presentation.model.UiText
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_band_high
import tuindice.assistant.generated.resources.assistant_card_b_band_low
import tuindice.assistant.generated.resources.assistant_card_b_band_medium
import tuindice.assistant.generated.resources.assistant_card_b_band_very_high
import tuindice.assistant.generated.resources.assistant_card_b_evaluation_graded
import tuindice.assistant.generated.resources.assistant_card_b_evaluation_overdue
import tuindice.assistant.generated.resources.assistant_card_b_evaluation_pending
import tuindice.assistant.generated.resources.assistant_card_b_subject_status_approved
import tuindice.assistant.generated.resources.assistant_card_b_subject_status_available
import tuindice.assistant.generated.resources.assistant_card_b_subject_status_blocked
import tuindice.assistant.generated.resources.assistant_card_b_subject_status_in_progress

// The words of the codes the academic cards bring in place of a word: the state of an evaluation, the status of a
// subject in the pensum, and a band of difficulty or of load. The codes are the serial names the service sends. A code
// this build does not know has no word, so the card shows no chip for it.

// The state of an evaluation: graded, pending or overdue.
fun evaluationStateText(code: String?): UiText? =
	when (code) {
		"graded", "completed" -> UiText.Resource(Res.string.assistant_card_b_evaluation_graded)
		"pending" -> UiText.Resource(Res.string.assistant_card_b_evaluation_pending)
		"overdue" -> UiText.Resource(Res.string.assistant_card_b_evaluation_overdue)
		else -> null
	}

// The status of a subject in the pensum: approved, in progress, available or blocked.
fun subjectStatusText(code: String?): UiText? =
	when (code) {
		"approved" -> UiText.Resource(Res.string.assistant_card_b_subject_status_approved)
		"current", "in_progress" -> UiText.Resource(Res.string.assistant_card_b_subject_status_in_progress)
		"available" -> UiText.Resource(Res.string.assistant_card_b_subject_status_available)
		"blocked" -> UiText.Resource(Res.string.assistant_card_b_subject_status_blocked)
		else -> null
	}

// A band of difficulty or of load: low, medium, high or very_high. Both read with the same feminine words.
fun bandText(code: String?): UiText? =
	when (code) {
		"low" -> UiText.Resource(Res.string.assistant_card_b_band_low)
		"medium" -> UiText.Resource(Res.string.assistant_card_b_band_medium)
		"high" -> UiText.Resource(Res.string.assistant_card_b_band_high)
		"very_high" -> UiText.Resource(Res.string.assistant_card_b_band_very_high)
		else -> null
	}
