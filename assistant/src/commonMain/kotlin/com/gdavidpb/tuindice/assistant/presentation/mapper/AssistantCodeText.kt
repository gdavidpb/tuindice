package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.CardChipItem
import com.gdavidpb.tuindice.assistant.presentation.model.CardChipTone
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
// not know has no word, so the card shows no chip for it. The tone of each chip comes from its code.

// The result of a subject. "unreported" has no word here, so a subject without a result shows no chip.
fun attemptOutcomeChip(code: String?): CardChipItem? =
	when (code) {
		"approved" -> chatCardChip(Res.string.assistant_card_a_outcome_approved, CardChipTone.Approved)
		"failed" -> chatCardChip(Res.string.assistant_card_a_outcome_failed, CardChipTone.Failed)
		"retired" -> chatCardChip(Res.string.assistant_card_a_outcome_retired, CardChipTone.Muted)
		"pending" -> chatCardChip(Res.string.assistant_card_a_outcome_pending, CardChipTone.Current)
		else -> null
	}

// The badge of a subject. "none" has no chip; an equivalence is a result that counts, as an approval does.
fun attemptBadgeChip(code: String?): CardChipItem? =
	when (code) {
		"without_effect" -> chatCardChip(Res.string.assistant_card_a_badge_without_effect, CardChipTone.Neutral)
		"equivalence" -> chatCardChip(Res.string.assistant_card_a_badge_equivalence, CardChipTone.Approved)
		else -> null
	}

// The state of a sync source, as the row of a source shows it. A source that answered reads in the success tone and one
// that did not, a problem of the sync, in the error tone, as the sync icon of the Summary; the rest read neutral.
fun syncSourceChip(code: String?): CardChipItem? =
	when (code) {
		"success" -> chatCardChip(Res.string.assistant_card_a_source_success, CardChipTone.Success)
		"unavailable" -> chatCardChip(Res.string.assistant_card_a_source_unavailable, CardChipTone.Failed)
		"not_attempted" -> chatCardChip(Res.string.assistant_card_a_source_not_attempted, CardChipTone.Neutral)
		"not_enrolled" -> chatCardChip(Res.string.assistant_card_a_source_not_enrolled, CardChipTone.Neutral)
		else -> null
	}

// Only an unavailable source is a problem of the sync. The rest reads as nothing to report, as the sync icon of the
// Summary does: an enrollment that is not in force is not a fault of the sync.
fun isSyncProblem(code: String?): Boolean = code == "unavailable"
