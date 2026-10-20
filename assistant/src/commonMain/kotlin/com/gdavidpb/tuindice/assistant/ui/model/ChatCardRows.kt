package com.gdavidpb.tuindice.assistant.ui.model

// How many rows a list card draws in a conversation. A list past it links the rest on its sheet, which draws them all.
internal const val CHAT_CARD_ROW_LIMIT = 5

// The rows a list card draws: its first [rowLimit] rows, or all of them when the limit is null, which is the card's own
// sheet.
internal fun <T> List<T>.visibleRows(rowLimit: Int?): List<T> = rowLimit?.let { limit -> take(limit) } ?: this
