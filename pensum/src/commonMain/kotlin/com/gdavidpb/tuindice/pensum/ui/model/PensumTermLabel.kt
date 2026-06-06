package com.gdavidpb.tuindice.pensum.ui.model

internal fun pensumTermOrdinalLabel(
	number: Int,
	shouldIncludeText: Boolean
): String {
	val ordinal = "$number°"
	return if (shouldIncludeText) {
		"$ordinal trimestre"
	} else {
		ordinal
	}
}
