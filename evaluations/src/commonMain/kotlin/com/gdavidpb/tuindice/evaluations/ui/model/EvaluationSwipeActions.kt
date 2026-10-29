package com.gdavidpb.tuindice.evaluations.ui.model

import androidx.compose.runtime.Immutable

// The actions a row offers. onAsk is null where the assistant is not provided: the row then offers no question.
@Immutable
data class EvaluationSwipeActions(
	val onAsk: (() -> Unit)?,
	val onEdit: () -> Unit,
	val onDelete: () -> Unit
)
