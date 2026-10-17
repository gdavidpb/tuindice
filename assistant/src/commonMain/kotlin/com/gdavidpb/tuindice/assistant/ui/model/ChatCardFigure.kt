package com.gdavidpb.tuindice.assistant.ui.model

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFigure
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

// The big figure of an index in a card: the index with its decimal comma, the [caption] under it, and the sentence a
// screen reader says for it, which is the [description] with the index in it («Índice del trimestre: 3,8125»).
@Composable
fun Double.toCardIndexFigure(
	caption: StringResource,
	description: StringResource
): CardFigure {
	val value = toCardIndexText()

	return CardFigure(
		value = value,
		caption = stringResource(caption),
		contentDescription = stringResource(description, value)
	)
}
