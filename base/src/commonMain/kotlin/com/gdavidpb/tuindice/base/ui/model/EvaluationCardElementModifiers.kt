package com.gdavidpb.tuindice.base.ui.model

import androidx.compose.ui.Modifier

// The modifiers of the parts of an evaluation card that a screen tags or makes tappable: its type icon, its place
// lines, its grade box and its state box. The card itself takes the modifier of its row, which carries the row's tap.
data class EvaluationCardElementModifiers(
	val typeIcon: Modifier = Modifier,
	val place: Modifier = Modifier,
	val grade: Modifier = Modifier,
	val status: Modifier = Modifier
)
