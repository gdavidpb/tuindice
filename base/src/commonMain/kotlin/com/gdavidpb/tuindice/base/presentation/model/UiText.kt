package com.gdavidpb.tuindice.base.presentation.model

import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.StringArrayResource
import org.jetbrains.compose.resources.StringResource

// Immutable so a presentation item that carries one stays stable: Compose compares it by value and
// skips a row whose texts did not change.
@Immutable
sealed interface UiText {
	data object Empty : UiText

	data class Raw(
		val value: String
	) : UiText

	data class Resource(
		val resource: StringResource,
		val args: List<Any> = emptyList()
	) : UiText

	// One element of a string-array, by its position: the name of a month or of a weekday.
	data class ArrayItem(
		val resource: StringArrayResource,
		val index: Int
	) : UiText

	// The text with its first letter in upper case: "jueves — 15/01/26" reads "Jueves — 15/01/26".
	data class Capitalized(
		val text: UiText
	) : UiText

	// The text in upper case, the way the week strip shows the short name of a day.
	data class Uppercase(
		val text: UiText
	) : UiText
}
