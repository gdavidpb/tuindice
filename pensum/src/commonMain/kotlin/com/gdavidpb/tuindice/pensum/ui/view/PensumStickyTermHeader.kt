package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.BorderStroke
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

			Surface(
				modifier = Modifier
					.offset(x = with(density) { xPx.toDp() })
					.width(with(density) { widthPx.toDp() })
					.fillMaxHeight()
					.padding(horizontal = 4.dp, vertical = 5.dp),
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
						text = trimesterOrdinalLabel(index + 1),
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

private fun trimesterOrdinalLabel(number: Int): String {
	val suffix = when (number) {
		1 -> "er"
		2 -> "do"
		3 -> "er"
		4 -> "to"
		5 -> "to"
		6 -> "to"
		7 -> "mo"
		8 -> "vo"
		9 -> "no"
		10 -> "mo"
		else -> when {
			number % 10 == 1 -> "er"
			number % 10 == 2 -> "do"
			number % 10 == 3 -> "er"
			else -> "to"
		}
	}
	return "$number$suffix trimestre"
}
