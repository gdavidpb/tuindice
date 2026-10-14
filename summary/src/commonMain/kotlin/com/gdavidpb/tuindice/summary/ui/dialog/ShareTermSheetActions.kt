package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.ui.graphics.ImageBitmap

/**
 * What the share sheet hands on when the student acts in it: the term that was tapped, the background that was tapped,
 * and the captured card when it is shared.
 */
data class ShareTermSheetActions(
	val onTermClick: (termKey: String) -> Unit,
	val onBackgroundClick: (backgroundId: String) -> Unit,
	val onShareClick: (bitmap: ImageBitmap) -> Unit
)
