package com.gdavidpb.tuindice.base.ui.view

/**
 * The words of a [RichTooltipBox], already formatted: an optional [title] and the body [lines]. [testTag]
 * marks the body so a test can find what the tooltip is showing: the line itself when the body is a single
 * line, the stack of lines otherwise.
 */
data class RichTooltipText(
	val lines: List<String>,
	val title: String? = null,
	val testTag: String? = null
)
