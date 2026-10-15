package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.presentation.model.ShareableTermCardItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

// The card is shown at 232 by 348 dp, which keeps its proportions (360 by 540 dp) and fits the sheet.
private val PreviewWidth = 232.dp
private val PreviewHeight = 348.dp
private val PreviewShape = RoundedCornerShape(12.dp)

/**
 * The card of a term, scaled down to the sheet. The card is laid out at the density its picture is taken at, so what
 * the student sees is the picture that is shared, only smaller. It stays in place as the term or the background
 * changes (see [ShareableTermCard]); the picture itself is taken by [ShareableTermCardCapture] when the student asks to
 * share.
 */
@Composable
fun ShareTermPreview(
	card: ShareableTermCardItem,
	background: ShareBackground,
	modifier: Modifier = Modifier
) {
	val density = LocalDensity.current
	// The card lays out at the density it is captured at: its box takes exactly the pixels of the shared picture on
	// this screen, and the scale brings those pixels down to the preview's width.
	val cardWidthPx = ShareableTermCardSize.width.value * SHARE_DENSITY
	val cardHeightPx = ShareableTermCardSize.height.value * SHARE_DENSITY
	val cardSize = with(density) { DpSize(width = cardWidthPx.toDp(), height = cardHeightPx.toDp()) }
	val scale = with(density) { PreviewWidth.toPx() } / cardWidthPx

	Box(
		modifier = modifier
			.size(width = PreviewWidth, height = PreviewHeight)
			.shadow(elevation = 4.dp, shape = PreviewShape)
			.clip(PreviewShape)
			.testTag(SummaryUiTags.ShareTermPreview)
	) {
		Box(
			modifier = Modifier
				// A box larger than the preview would be centred on it; held at the top start, the scale around that corner
				// lays the whole card over the preview.
				.wrapContentSize(align = Alignment.TopStart, unbounded = true)
				.requiredSize(cardSize)
				.graphicsLayer {
					scaleX = scale
					scaleY = scale
					transformOrigin = TransformOrigin(0f, 0f)
				}
		) {
			CompositionLocalProvider(LocalDensity provides Density(density = SHARE_DENSITY, fontScale = 1f)) {
				ShareableTermCard(
					item = card,
					background = background
				)
			}
		}
	}
}
