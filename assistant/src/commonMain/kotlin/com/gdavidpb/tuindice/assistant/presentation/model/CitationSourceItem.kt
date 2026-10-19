package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// One cited source: its document and article, its excerpt and, when the service brings one, the official document.
data class CitationSourceItem(
	val documentText: UiText,
	val article: String,
	val excerpt: String,
	val url: String?
)
