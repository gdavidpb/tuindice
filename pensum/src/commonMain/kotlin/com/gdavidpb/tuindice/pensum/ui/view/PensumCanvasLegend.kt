package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_canvas_legend_approved
import tuindice.pensum.generated.resources.pensum_canvas_legend_available
import tuindice.pensum.generated.resources.pensum_canvas_legend_blocked
import tuindice.pensum.generated.resources.pensum_canvas_legend_current

@Composable
fun PensumCanvasLegend(
	modifier: Modifier = Modifier
) {
	val items = listOf(
		LegendItem(
			label = Res.string.pensum_canvas_legend_approved,
			color = Approved
		),
		LegendItem(
			label = Res.string.pensum_canvas_legend_current,
			color = Current
		),
		LegendItem(
			label = Res.string.pensum_canvas_legend_available,
			color = Available
		),
		LegendItem(
			label = Res.string.pensum_canvas_legend_blocked,
			color = Available.copy(alpha = 0.72f),
			isLocked = true
		)
	)

	Surface(
		modifier = modifier
			.fillMaxWidth()
			.padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 8.dp)
			.testTag(PensumUiTags.CanvasLegend),
		shape = RoundedCornerShape(8.dp),
		color = FloatingPanelBackground,
		border = BorderStroke(1.dp, PanelBorder.copy(alpha = 0.9f))
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.height(38.dp)
				.horizontalScroll(rememberScrollState())
				.padding(horizontal = 10.dp),
			horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
			verticalAlignment = Alignment.CenterVertically
		) {
			items.forEach { item ->
				PensumCanvasLegendItem(item = item)
			}
		}
	}
}

@Composable
private fun PensumCanvasLegendItem(
	item: LegendItem
) {
	Row(
		horizontalArrangement = Arrangement.spacedBy(5.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		PensumCanvasLegendMarker(item = item)
		Text(
			text = stringResource(item.label),
			style = MaterialTheme.typography.labelSmall,
			fontWeight = FontWeight.SemiBold,
			color = TextPrimary.copy(alpha = 0.86f),
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)
	}
}

@Composable
private fun PensumCanvasLegendMarker(
	item: LegendItem
) {
	if (item.isLocked) {
		Icon(
			imageVector = Icons.Outlined.Lock,
			contentDescription = null,
			tint = item.color,
			modifier = Modifier.size(14.dp)
		)
	} else {
		Box(
			modifier = Modifier
				.size(10.dp)
				.background(item.color)
				.border(1.dp, TextPrimary.copy(alpha = 0.12f))
		)
	}
}

private data class LegendItem(
	val label: StringResource,
	val color: Color,
	val isLocked: Boolean = false
)
