package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.ReportComposition
import com.gdavidpb.tuindice.pensum.presentation.model.PensumTermItem
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.pensum.ui.model.pensumTermOrdinalLabel
import kotlin.math.max
import kotlin.math.roundToInt

// The canvas's scale and offset are read in layout, where each term is placed and sized: panning and zooming move the
// terms without recomposing the header. The caller says whether the labels are full (a state it derives from the
// scale), so the header recomposes only when they switch between short and full.
@Composable
fun PensumStickyTermHeader(
	terms: List<PensumTermItem>,
	scale: () -> Float,
	offsetX: () -> Float,
	shouldUseFullLabels: Boolean,
	densityScale: Float,
	onTermClick: (termId: String) -> Unit,
	modifier: Modifier = Modifier
) {
	ReportComposition("header")
	val graphColors = pensumGraphColors()
	val density = LocalDensity.current
	val labelTextStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
	val minimumWidthPx = with(density) {
		if (shouldUseFullLabels) {
			StickyTermFullLabelMinWidth.toPx()
		} else {
			StickyTermShortLabelMinWidth.toPx()
		}
	}

	Box(
		modifier = modifier
			.fillMaxWidth()
			.height(StickyTermHeaderHeight)
			.clipToBounds()
			.testTag(PensumUiTags.StickyTerms)
	) {
		terms.forEachIndexed { index, term ->
			val labelHorizontalPadding = if (shouldUseFullLabels) {
				StickyTermFullLabelHorizontalPadding
			} else {
				StickyTermShortLabelHorizontalPadding
			}
			val label = pensumTermOrdinalLabel(
				number = index + 1,
				shouldIncludeText = shouldUseFullLabels
			)

			Surface(
				modifier = Modifier
					.layout { measurable, constraints ->
						val currentScale = scale()
						val widthPx = constraints.constrainWidth(
							max(term.width.toFloat() * densityScale * currentScale, minimumWidthPx).roundToInt()
						)
						val placeable = measurable.measure(constraints.copy(minWidth = widthPx, maxWidth = widthPx))
						layout(placeable.width, placeable.height) {
							placeable.placeRelative(
								x = (offsetX() + term.x.toFloat() * densityScale * currentScale).roundToInt(),
								y = 0
							)
						}
					}
					.fillMaxHeight()
					.padding(horizontal = StickyTermOuterHorizontalPadding, vertical = 5.dp)
					.clickable { onTermClick(term.id) },
				shape = PensumElementShape,
				color = graphColors.floatingPanelBackground,
				border = BorderStroke(1.dp, graphColors.panelBorder.copy(alpha = 0.9f))
			) {
				Box(
					modifier = Modifier.fillMaxSize(),
					contentAlignment = Alignment.Center
				) {
					Text(
						modifier = Modifier.padding(horizontal = labelHorizontalPadding),
						text = label,
						textAlign = TextAlign.Center,
						style = labelTextStyle,
						fontWeight = FontWeight.SemiBold,
						color = graphColors.textPrimary,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis
					)
				}
			}
		}
	}
}

private val StickyTermOuterHorizontalPadding = 4.dp
private val StickyTermFullLabelHorizontalPadding = 8.dp
private val StickyTermShortLabelHorizontalPadding = 4.dp
