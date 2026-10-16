package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermBackgroundItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

/**
 * The backgrounds of the card as a strip of thumbnails, each with its short name above it: the artwork alone, as in a
 * photo filter. The chosen one wears a ring in the primary colour, and a tap on any other one moves the card to it.
 * The one of [loadingId], whose picture the card still waits for, is marked as loading.
 */
@Composable
fun ShareTermBackgroundStrip(
	backgrounds: List<ShareTermBackgroundItem>,
	onBackgroundClick: (backgroundId: String) -> Unit,
	modifier: Modifier = Modifier,
	loadingId: String? = null
) {
	LazyRow(
		modifier = modifier.testTag(SummaryUiTags.ShareTermBackgrounds),
		horizontalArrangement = Arrangement.spacedBy(12.dp)
	) {
		items(items = backgrounds, key = { item -> item.artwork.id }) { item ->
			ShareTermBackgroundThumb(
				item = item,
				onClick = { onBackgroundClick(item.artwork.id) },
				isLoading = item.artwork.id == loadingId
			)
		}
	}
}
