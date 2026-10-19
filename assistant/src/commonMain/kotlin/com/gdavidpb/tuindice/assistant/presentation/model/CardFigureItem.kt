package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// A figure a chat card shows big: its formatted value, the caption under it, and the sentence a screen reader says for
// it. The texts are ready to show; the view turns them into strings where it draws the figure.
data class CardFigureItem(
	val value: UiText,
	val caption: UiText,
	val contentDescription: UiText
)
