package com.gdavidpb.tuindice.summary.presentation.model

/** The icon a card of the hub wears: what the card is about. */
enum class SummaryCardIcon {
	/** The evaluations: the invitation to add the first, or that they have not been downloaded yet. */
	Evaluations,

	/** A check: nothing is pending. */
	Done,

	/** A calendar: the week has nothing to show. */
	Calendar,

	/** A document with a plus: a term to plan, or the first one to come. */
	NewTerm,

	/** The pensum: the invitation to open it, when no pensum was ever calculated. */
	Career
}
