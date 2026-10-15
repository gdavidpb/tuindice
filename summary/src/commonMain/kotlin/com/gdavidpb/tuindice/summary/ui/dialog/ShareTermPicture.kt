package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermArtwork
import com.gdavidpb.tuindice.summary.presentation.model.ShareableTermCardItem
import com.gdavidpb.tuindice.summary.ui.share.ShareableTermCardCapture

/**
 * The picture of the card while the sheet waits for it, handed on once when it is taken. The card is drawn into the
 * picture only: it sits in a box of no size, without semantics of its own, so the sheet shows and announces the card
 * once.
 */
@Composable
fun ShareTermPicture(
	card: ShareableTermCardItem,
	artwork: ShareTermArtwork,
	onCaptured: (ImageBitmap) -> Unit
) {
	Box(modifier = Modifier.size(0.dp).clearAndSetSemantics { }) {
		ShareableTermCardCapture(
			item = card,
			artwork = artwork,
			onCaptured = onCaptured
		)
	}
}
