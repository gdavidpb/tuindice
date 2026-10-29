package com.gdavidpb.tuindice.assistant.presentation.model

// The tone of a state chip of a card. The mapper gives it from the state the contract brings, never from the words of
// the chip, and the view paints the chip in it. Neutral is the plain outlined chip of a state that has no verdict.
enum class CardChipTone {
	Neutral,
	Approved,
	Current,
	Failed,
	Success,
	Warning,
	Muted
}
