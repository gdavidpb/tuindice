package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermBackgroundItem

// The thumbnail is 58 by 87 dp, as the artwork's own proportions give it, and its ring sits 3 dp around it.
private val ThumbWidth: Dp = 58.dp
private val ThumbHeight: Dp = 87.dp
private val RingWidth: Dp = 3.dp
private val FrameWidth: Dp = ThumbWidth + RingWidth * 2
private val FrameHeight: Dp = ThumbHeight + RingWidth * 2
private val ThumbShape = RoundedCornerShape(TuIndiceRadius.Medium)

// The ring follows the thumbnail's corner, out by its own width, so the two curves stay concentric.
private val RingShape = RoundedCornerShape(TuIndiceRadius.Medium + RingWidth)

/**
 * The tile of one background: its short name above its thumbnail, with the ring of the primary colour when it is
 * chosen. The whole tile is one radio button, so a tap on it chooses the background.
 */
@Composable
fun ShareTermBackgroundTile(
	item: ShareTermBackgroundItem,
	onClick: () -> Unit
) {
	Column(
		modifier = Modifier
			.width(FrameWidth)
			.testTag(ShareTermUiTags.background(item.artwork.id))
			.selectable(
				selected = item.selected,
				onClick = onClick,
				role = Role.RadioButton
			),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text(
			text = item.artwork.shortName,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			style = MaterialTheme.typography.labelSmall,
			fontWeight = if (item.selected) FontWeight.SemiBold else FontWeight.Normal,
			color = MaterialTheme.colorScheme.onSurface
		)

		Box(
			modifier = Modifier
				.padding(top = 4.dp)
				.size(width = FrameWidth, height = FrameHeight)
				.then(
					if (item.selected) {
						Modifier
							.border(
								width = RingWidth,
								color = MaterialTheme.colorScheme.primary,
								shape = RingShape
							)
							.testTag(ShareTermUiTags.backgroundRing(item.artwork.id))
					} else {
						Modifier
					}
				),
			contentAlignment = Alignment.Center
		) {
			Box(
				modifier = Modifier
					.size(width = ThumbWidth, height = ThumbHeight)
					.clip(ThumbShape)
			) {
				ShareTermThumbnail(art = item.artwork.art)
			}
		}
	}
}
