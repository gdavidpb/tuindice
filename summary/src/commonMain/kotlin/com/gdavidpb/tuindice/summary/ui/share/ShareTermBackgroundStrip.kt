package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import com.gdavidpb.tuindice.summary.domain.model.ShareBackgroundArt
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermBackgroundItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import org.jetbrains.compose.resources.painterResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.share_background_laberinto_cromovegetal_thumb

// The thumbnail is 58 by 87 dp, as the artwork's own proportions give it, and its ring sits 3 dp around it.
private val ThumbWidth: Dp = 58.dp
private val ThumbHeight: Dp = 87.dp
private val RingWidth: Dp = 3.dp
private val FrameWidth: Dp = ThumbWidth + RingWidth * 2
private val FrameHeight: Dp = ThumbHeight + RingWidth * 2
private val ThumbShape = RoundedCornerShape(10.dp)
private val RingShape = RoundedCornerShape(13.dp)

/**
 * The backgrounds of the card as a strip of thumbnails, each with its short name above it: the artwork alone, as in a
 * photo filter. The chosen one wears a ring in the primary colour, and a tap on any other one moves the card to it.
 */
@Composable
fun ShareTermBackgroundStrip(
	backgrounds: List<ShareTermBackgroundItem>,
	onBackgroundClick: (backgroundId: String) -> Unit,
	modifier: Modifier = Modifier
) {
	LazyRow(
		modifier = modifier.testTag(SummaryUiTags.ShareTermBackgrounds),
		horizontalArrangement = Arrangement.spacedBy(12.dp)
	) {
		items(items = backgrounds, key = { item -> item.background.id }) { item ->
			ShareTermBackgroundThumb(
				item = item,
				onClick = { onBackgroundClick(item.background.id) }
			)
		}
	}
}

@Composable
private fun ShareTermBackgroundThumb(
	item: ShareTermBackgroundItem,
	onClick: () -> Unit
) {
	Column(
		modifier = Modifier
			.width(FrameWidth)
			.testTag(ShareTermUiTags.background(item.background.id))
			.selectable(
				selected = item.selected,
				onClick = onClick,
				role = Role.RadioButton
			),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text(
			text = item.background.shortName,
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
							.testTag(ShareTermUiTags.backgroundRing(item.background.id))
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
				ShareTermThumbnail(art = item.background.art)
			}
		}
	}
}

@Composable
private fun ShareTermThumbnail(art: ShareBackgroundArt) {
	when (art) {
		ShareBackgroundArt.Bundled ->
			Image(
				painter = painterResource(Res.drawable.share_background_laberinto_cromovegetal_thumb),
				contentDescription = null,
				modifier = Modifier.fillMaxSize(),
				contentScale = ContentScale.Crop
			)

		is ShareBackgroundArt.Remote -> {
			val platformContext = LocalPlatformContext.current
			// The app-wide loader, the same one the card reads: its memory holds the thumbnails between visits.
			val imageLoader = remember(platformContext) { SingletonImageLoader.get(platformContext) }

			AsyncImage(
				model = art.thumbUrl,
				imageLoader = imageLoader,
				contentDescription = null,
				// A thumbnail that cannot be loaded stays the frame's plain surface, never another work's picture.
				modifier = Modifier
					.fillMaxSize()
					.background(MaterialTheme.colorScheme.surfaceVariant),
				contentScale = ContentScale.Crop
			)
		}
	}
}
