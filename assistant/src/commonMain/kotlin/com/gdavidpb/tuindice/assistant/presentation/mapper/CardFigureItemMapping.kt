package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.CardFigureItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import org.jetbrains.compose.resources.StringResource

// The figure of an index: the index with its decimal point, the caption under it, and the sentence a screen reader
// says for it, which is the description with the index in it («Índice del trimestre: 3.8125»).
internal fun Double.toCardIndexFigureItem(caption: StringResource, description: StringResource): CardFigureItem {
	val value = UiText.Raw(toCardIndexText())

	return CardFigureItem(
		value = value,
		caption = UiText.Resource(caption),
		contentDescription = UiText.Resource(description, listOf(value))
	)
}
