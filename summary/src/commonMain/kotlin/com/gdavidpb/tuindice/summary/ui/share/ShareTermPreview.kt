package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.presentation.model.ShareableTermCardItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

// The card is shown at 232 by 348 dp, which keeps its proportions (360 by 540 dp) and fits the sheet.
private val PreviewWidth = 232.dp
private val PreviewHeight = 348.dp
private val PreviewShape = RoundedCornerShape(12.dp)

/**
 * The card of a term, scaled down to the sheet. The card is captured where it is shown, so the picture that is
 * shared is the one the student sees: [onCaptured] receives its bitmap once its artwork has loaded.
 */
@Composable
fun ShareTermPreview(
	card: ShareableTermCardItem,
	background: ShareBackground,
	onCaptured: (bitmap: ImageBitmap) -> Unit,
	modifier: Modifier = Modifier
) {
	val density = LocalDensity.current
	// The card lays out at the density it is captured at, so its width in those pixels is what the scale comes from.
	val scale = with(density) { PreviewWidth.toPx() } / (ShareableTermCardSize.width.value * SHARE_DENSITY)

	Box(
		modifier = modifier
			.size(width = PreviewWidth, height = PreviewHeight)
			.shadow(elevation = 4.dp, shape = PreviewShape)
			.clip(PreviewShape)
			.testTag(SummaryUiTags.ShareTermPreview)
	) {
		Box(
			modifier = Modifier
				.requiredSize(ShareableTermCardSize)
				.graphicsLayer {
					scaleX = scale
					scaleY = scale
					transformOrigin = TransformOrigin(0f, 0f)
				}
		) {
			// A card is captured for each term and background: the capture of another one is not this card's.
			key(card, background.id) {
				ShareableTermCardCapture(
					item = card,
					background = background,
					onCaptured = onCaptured
				)
			}
		}
	}
}
