package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// A state of a chat card: the word the chip shows and the tone it is painted in.
data class CardChipItem(
	val text: UiText,
	val tone: CardChipTone
)
