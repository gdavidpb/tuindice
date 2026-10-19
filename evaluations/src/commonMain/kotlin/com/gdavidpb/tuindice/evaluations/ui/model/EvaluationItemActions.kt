package com.gdavidpb.tuindice.evaluations.ui.model

// What one evaluation's card does: its grade button, the card itself, and its edit, delete and ask actions. The edit,
// the delete and the ask are null where the card offers no such action.
data class EvaluationItemActions(
	val onGradeClick: () -> Unit = {},
	val onCardClick: () -> Unit = {},
	val onEditAction: (() -> Unit)? = null,
	val onDeleteAction: (() -> Unit)? = null,
	val onAskAssistant: (() -> Unit)? = null
)
