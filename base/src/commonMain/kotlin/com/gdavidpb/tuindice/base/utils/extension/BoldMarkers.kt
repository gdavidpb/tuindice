package com.gdavidpb.tuindice.base.utils.extension

private const val BOLD_MARKER = "**"

// Walks a text with `**bold**` markers, piece by piece. A marker without its closing pair stays as text.
fun String.forEachBoldMarkerSegment(onSegment: (text: String, isBold: Boolean) -> Unit) {
	var currentIndex = 0

	while (currentIndex < length) {
		val opening = indexOf(BOLD_MARKER, currentIndex)
		val closing = if (opening >= 0) indexOf(BOLD_MARKER, opening + BOLD_MARKER.length) else -1
		if (closing < 0) {
			onSegment(substring(currentIndex), false)
			return
		}

		onSegment(substring(currentIndex, opening), false)
		onSegment(substring(opening + BOLD_MARKER.length, closing), true)
		currentIndex = closing + BOLD_MARKER.length
	}
}

// The text as it reads on screen, for what leaves the app as plain text.
fun String.withoutBoldMarkers(): String = buildString {
	forEachBoldMarkerSegment { segment, _ -> append(segment) }
}
