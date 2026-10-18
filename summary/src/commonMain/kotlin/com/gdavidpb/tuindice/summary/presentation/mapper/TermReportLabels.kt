package com.gdavidpb.tuindice.summary.presentation.mapper

// The name of a term as a screen writes it where a line break must not split it, "Ene - Mar 2026": its spaces do not
// break, and an invisible word joiner on each side of the dash keeps the line from breaking after it, so the name stays
// whole on one line.
internal fun termNameUnbreakable(label: String): String = label
	.replace(' ', NO_BREAK_SPACE)
	.replace("-", "$WORD_JOINER-$WORD_JOINER")

private const val NO_BREAK_SPACE = ' '

private const val WORD_JOINER = '\u2060'
