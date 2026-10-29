package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.CardChipItem
import com.gdavidpb.tuindice.assistant.presentation.model.CardChipTone
import com.gdavidpb.tuindice.base.presentation.model.UiText
import org.jetbrains.compose.resources.StringResource

// A state chip of a card: its word from the resources, and the tone the mapper gives the state it names.
internal fun chatCardChip(text: StringResource, tone: CardChipTone): CardChipItem =
	CardChipItem(text = UiText.Resource(text), tone = tone)
