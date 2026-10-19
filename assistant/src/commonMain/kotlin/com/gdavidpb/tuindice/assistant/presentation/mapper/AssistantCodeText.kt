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

// The words of the codes a card brings in place of a word: the result of a subject, its badge and the state of a sync
// source. The codes are the serial names of AttemptOutcome, AttemptBadge and SyncSourceStatus. A code this build does
// not know has no word, so the card shows no chip for it.

// The result of a subject. "unreported" has no word here, so a subject without a result shows no chip.
fun attemptOutcomeText(code: String?): UiText? =
	when (code) {
		"approved" -> UiText.Resource(Res.string.assistant_card_a_outcome_approved)
		"failed" -> UiText.Resource(Res.string.assistant_card_a_outcome_failed)
		"retired" -> UiText.Resource(Res.string.assistant_card_a_outcome_retired)
		"pending" -> UiText.Resource(Res.string.assistant_card_a_outcome_pending)
		else -> null
	}

// The badge of a subject. "none" has no chip.
fun attemptBadgeText(code: String?): UiText? =
	when (code) {
		"without_effect" -> UiText.Resource(Res.string.assistant_card_a_badge_without_effect)
		"equivalence" -> UiText.Resource(Res.string.assistant_card_a_badge_equivalence)
		else -> null
	}

// The state of a sync source, as the row of a source shows it.
fun syncSourceText(code: String?): UiText? =
	when (code) {
		"success" -> UiText.Resource(Res.string.assistant_card_a_source_success)
		"unavailable" -> UiText.Resource(Res.string.assistant_card_a_source_unavailable)
		"not_attempted" -> UiText.Resource(Res.string.assistant_card_a_source_not_attempted)
		"not_enrolled" -> UiText.Resource(Res.string.assistant_card_a_source_not_enrolled)
		else -> null
	}

// Only an unavailable source is a problem of the sync. The rest reads as nothing to report, as the sync icon of the
// Summary does: an enrollment that is not in force is not a fault of the sync.
fun isSyncProblem(code: String?): Boolean = code == "unavailable"
