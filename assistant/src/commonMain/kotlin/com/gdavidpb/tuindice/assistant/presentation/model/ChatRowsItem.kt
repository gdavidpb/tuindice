package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// A list card: one row per record. In a conversation it draws its first rows up to the row limit, and past it a link to
// its sheet for the rest, with [seeAllText] as its text. That link only shows past the limit, so its text always reads
// in the plural.
interface ChatRowsItem {
	val rowCount: Int
	val seeAllText: UiText
}
