package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermBackgroundItem

/**
 * One background of the strip: its tile, marked while [isLoading] its picture for the card, and, when it is the chosen
 * one, a box around the tile that carries the mark of the choice, a node of its own that the E2E driver reads.
 */
@Composable
fun ShareTermBackgroundThumb(
	item: ShareTermBackgroundItem,
	onClick: () -> Unit,
	isLoading: Boolean = false
) {
	Box(
		modifier = if (item.selected) {
			Modifier.testTag(ShareTermUiTags.backgroundSelected(item.artwork.id))
		} else {
			Modifier
		}
	) {
		ShareTermBackgroundTile(item = item, onClick = onClick, isLoading = isLoading)
	}
}
