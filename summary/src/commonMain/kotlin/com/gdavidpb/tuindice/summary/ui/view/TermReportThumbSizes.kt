package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.unit.dp

// The sizes of the term report thumbs, and the space their touch area leaves around each icon.
internal object TermReportThumbSizes {
	val ButtonSize = 48.dp
	val IconSize = 20.dp

	// The space the touch area leaves around each thumb's icon, on the side that meets the edge of the content.
	val TouchMargin = (ButtonSize - IconSize) / 2
}
