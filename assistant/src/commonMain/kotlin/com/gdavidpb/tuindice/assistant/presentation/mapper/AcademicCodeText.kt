package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.CardChipItem
import com.gdavidpb.tuindice.assistant.presentation.model.CardChipTone
import com.gdavidpb.tuindice.base.presentation.model.EvaluationTone
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_outcome_approved
import tuindice.assistant.generated.resources.assistant_card_a_outcome_pending
import tuindice.assistant.generated.resources.assistant_card_b_band_high
import tuindice.assistant.generated.resources.assistant_card_b_band_low
import tuindice.assistant.generated.resources.assistant_card_b_band_medium
import tuindice.assistant.generated.resources.assistant_card_b_band_very_high
import tuindice.assistant.generated.resources.assistant_card_b_evaluation_continuous
import tuindice.assistant.generated.resources.assistant_card_b_evaluation_graded
import tuindice.assistant.generated.resources.assistant_card_b_evaluation_overdue
import tuindice.assistant.generated.resources.assistant_card_b_evaluation_pending
import tuindice.assistant.generated.resources.assistant_card_b_subject_status_available
import tuindice.assistant.generated.resources.assistant_card_b_subject_status_blocked

// The words of the codes the academic cards bring in place of a word: the state of an evaluation, the status of a
// subject in the pensum, and a band of difficulty or of load. The codes are the serial names the service sends. A code
// this build does not know has no word, so the card shows no chip for it. The tone of each chip comes from its code.

// The state of an evaluation: the serial names of EvaluationState (completed, pending, overdue, continuous). Only an
// overdue one reads in the error tone, as the Evaluations screen marks it; the rest are no verdict and read neutral.
fun evaluationStateChip(code: String?): CardChipItem? =
	when (code) {
		"graded", "completed" -> chatCardChip(Res.string.assistant_card_b_evaluation_graded, CardChipTone.Neutral)
		"continuous" -> chatCardChip(Res.string.assistant_card_b_evaluation_continuous, CardChipTone.Neutral)
		"pending" -> chatCardChip(Res.string.assistant_card_b_evaluation_pending, CardChipTone.Neutral)
		"overdue" -> chatCardChip(Res.string.assistant_card_b_evaluation_overdue, CardChipTone.Failed)
		else -> null
	}

// The tone of an evaluation's state, drawn as the Evaluations tab draws it: a completed one is a success, an overdue
// one is an error, and the others have no verdict and read neutral. The rows use this tone, not the chip's own.
fun evaluationStateTone(code: String?): EvaluationTone =
	when (code) {
		"graded", "completed" -> EvaluationTone.Success
		"overdue" -> EvaluationTone.Error
		else -> EvaluationTone.Neutral
	}

// The status of a subject in the pensum: approved, in progress, available or blocked.
fun subjectStatusChip(code: String?): CardChipItem? =
	when (code) {
		"approved" -> chatCardChip(Res.string.assistant_card_a_outcome_approved, CardChipTone.Approved)
		"current", "in_progress" -> chatCardChip(Res.string.assistant_card_a_outcome_pending, CardChipTone.Current)
		"available" -> chatCardChip(Res.string.assistant_card_b_subject_status_available, CardChipTone.Muted)
		"blocked" -> chatCardChip(Res.string.assistant_card_b_subject_status_blocked, CardChipTone.Muted)
		else -> null
	}

// A band of difficulty: low, medium, high or very_high, as the subject statistics name it.
fun bandChip(code: String?): CardChipItem? =
	when (code) {
		"low" -> chatCardChip(Res.string.assistant_card_b_band_low, CardChipTone.Neutral)
		"medium" -> chatCardChip(Res.string.assistant_card_b_band_medium, CardChipTone.Neutral)
		"high" -> chatCardChip(Res.string.assistant_card_b_band_high, CardChipTone.Neutral)
		"very_high" -> chatCardChip(Res.string.assistant_card_b_band_very_high, CardChipTone.Neutral)
		else -> null
	}

// The load band of a plan. The service sends it as the word the app's load preview shows («Exigente», «Sin
// estimación»), so the card writes it as it comes; a blank one shows nothing.
fun planLoadBandText(band: String?): String? = band?.trim()?.takeIf { text -> text.isNotEmpty() }
