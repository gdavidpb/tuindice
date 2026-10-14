package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

/**
 * A share of a whole drawn as a thin track: the part of it up to [fraction] is filled in [color] and the rest is the
 * track. The figure or the row it sits in already says the same, so the track is decoration for a screen reader.
 */
@Composable
fun SummaryStatisticsTrackView(
	fraction: Float,
	color: Color,
	modifier: Modifier = Modifier
) {
	Box(
		modifier = modifier
			.fillMaxWidth()
			.height(TrackHeight)
			.clip(RoundedCornerShape(TuIndiceRadius.Full))
			.background(MaterialTheme.colorScheme.outlineVariant)
	) {
		Box(
			modifier = Modifier
				.fillMaxHeight()
				.fillMaxWidth(fraction.coerceIn(0f, 1f))
				.background(color)
				.testTag(SummaryUiTags.StatisticsTrackFill)
		)
	}
}

private val TrackHeight = 8.dp
