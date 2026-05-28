package com.gdavidpb.tuindice.base.presentation.model

import org.jetbrains.compose.resources.StringResource

sealed interface UiText {
	data object Empty : UiText

	data class Raw(
		val value: String
	) : UiText

	data class Resource(
		val resource: StringResource,
		val args: List<Any> = emptyList()
	) : UiText
}
