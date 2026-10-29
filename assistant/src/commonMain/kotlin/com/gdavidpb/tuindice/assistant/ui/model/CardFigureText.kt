package com.gdavidpb.tuindice.assistant.ui.model

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.assistant.presentation.model.CardFigureItem
import com.gdavidpb.tuindice.base.presentation.model.asString

// The figure as a card draws it: its texts read where they are drawn.
@Composable
fun CardFigureItem.toCardFigure(): CardFigure {
	return CardFigure(
		value = value.asString(),
		caption = caption.asString(),
		contentDescription = contentDescription.asString()
	)
}
