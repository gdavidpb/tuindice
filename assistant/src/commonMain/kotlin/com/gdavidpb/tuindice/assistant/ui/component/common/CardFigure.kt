package com.gdavidpb.tuindice.assistant.ui.component.common

// A figure a chat card shows big: its formatted [value], the [caption] under it and the full sentence a screen
// reader says for it («Índice del trimestre: 3,8125»).
data class CardFigure(
	val value: String,
	val caption: String,
	val contentDescription: String
)
