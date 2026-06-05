package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import kotlin.math.max

@Composable
fun PensumStickyTermHeader(
	terms: List<PensumScreenModel.Term>,
	scale: Float,
	offsetX: Float,
	densityScale: Float,
	onTermClick: (termId: String) -> Unit,
	modifier: Modifier = Modifier
) {
	val density = LocalDensity.current
	val minimumWidthPx = with(density) { StickyTermMinWidth.toPx() }

	Box(
		modifier = modifier
			.fillMaxWidth()
			.height(StickyTermHeaderHeight)
			.clipToBounds()
			.testTag(PensumUiTags.StickyTerms)
	) {
		terms.forEachIndexed { index, term ->
			val xPx = offsetX + term.x.toFloat() * densityScale * scale
			val widthPx = max(term.width.toFloat() * densityScale * scale, minimumWidthPx)
			val widthDp = with(density) { widthPx.toDp() }
			val label = trimesterOrdinalLabel(
				number = index + 1,
				shouldIncludeText = widthDp >= StickyTermFullLabelMinWidth
			)

			Surface(
				modifier = Modifier
					.offset(x = with(density) { xPx.toDp() })
					.width(widthDp)
					.fillMaxHeight()
					.padding(horizontal = 4.dp, vertical = 5.dp)
					.clickable { onTermClick(term.id) },
				shape = RoundedCornerShape(8.dp),
				color = FloatingPanelBackground,
				border = BorderStroke(1.dp, PanelBorder.copy(alpha = 0.9f))
			) {
				Box(
					modifier = Modifier.fillMaxSize(),
					contentAlignment = Alignment.Center
				) {
					Text(
						modifier = Modifier.padding(horizontal = 8.dp),
						text = label,
						textAlign = TextAlign.Center,
						style = MaterialTheme.typography.labelMedium,
						fontWeight = FontWeight.SemiBold,
						color = TextPrimary,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis
					)
				}
			}
		}
	}
}

private fun trimesterOrdinalLabel(number: Int, shouldIncludeText: Boolean): String {
	val ordinal = "$number°"
	return if (shouldIncludeText) {
		"$ordinal trimestre"
	} else {
		ordinal
	}
}
