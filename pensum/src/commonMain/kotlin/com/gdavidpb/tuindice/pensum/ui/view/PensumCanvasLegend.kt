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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusType
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.pensum.ui.model.PensumStatusIconVisual
import com.gdavidpb.tuindice.pensum.ui.model.toStatusIconVisual
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_canvas_filters_clear
import tuindice.pensum.generated.resources.pensum_canvas_legend_approved
import tuindice.pensum.generated.resources.pensum_canvas_legend_available
import tuindice.pensum.generated.resources.pensum_canvas_legend_blocked
import tuindice.pensum.generated.resources.pensum_canvas_legend_current

@Composable
fun PensumCanvasLegend(
	activeStatusFilters: Set<PensumNodeStatusType>,
	onStatusFilterToggle: (PensumNodeStatusType) -> Unit,
	onClearStatusFilters: () -> Unit,
	modifier: Modifier = Modifier
) {
	val items = listOf(
		LegendItem(
			statusType = PensumNodeStatusType.APPROVED,
			label = Res.string.pensum_canvas_legend_approved,
			color = Approved,
			icon = PensumNodeStatusType.APPROVED.toStatusIconVisual()
		),
		LegendItem(
			statusType = PensumNodeStatusType.CURRENT,
			label = Res.string.pensum_canvas_legend_current,
			color = Current,
			icon = PensumNodeStatusType.CURRENT.toStatusIconVisual()
		),
		LegendItem(
			statusType = PensumNodeStatusType.AVAILABLE,
			label = Res.string.pensum_canvas_legend_available,
			color = Available,
			icon = PensumNodeStatusType.AVAILABLE.toStatusIconVisual()
		),
		LegendItem(
			statusType = PensumNodeStatusType.BLOCKED,
			label = Res.string.pensum_canvas_legend_blocked,
			color = CanvasNeutral.copy(alpha = 0.72f),
			icon = PensumNodeStatusType.BLOCKED.toStatusIconVisual()
		)
	)

	Surface(
		modifier = modifier
			.fillMaxWidth()
			.padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 8.dp)
			.testTag(PensumUiTags.CanvasLegend),
		shape = PensumElementShape,
		color = FloatingPanelBackground,
		border = BorderStroke(1.dp, PanelBorder.copy(alpha = 0.9f))
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.height(38.dp)
				.padding(horizontal = 10.dp),
			horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
			verticalAlignment = Alignment.CenterVertically
		) {
			Row(
				modifier = Modifier
					.weight(1f)
					.horizontalScroll(rememberScrollState()),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				items.forEach { item ->
					PensumCanvasLegendItem(
						item = item,
						isSelected = item.statusType in activeStatusFilters,
						onClick = { onStatusFilterToggle(item.statusType) }
					)
				}
			}
			if (activeStatusFilters.isNotEmpty()) {
				PensumCanvasLegendClearButton(onClick = onClearStatusFilters)
			}
		}
	}
}

@Composable
private fun PensumCanvasLegendItem(
	item: LegendItem,
	isSelected: Boolean,
	onClick: () -> Unit
) {
	Surface(
		modifier = Modifier
			.height(28.dp)
			.selectable(
				selected = isSelected,
				role = Role.Checkbox,
				onClick = onClick
			)
			.testTag(PensumUiTags.statusFilter(item.statusType)),
		shape = PensumElementShape,
		color = if (isSelected) item.color.copy(alpha = 0.16f) else Color.Transparent,
		border = BorderStroke(1.dp, if (isSelected) item.color.copy(alpha = 0.82f) else Color.Transparent)
	) {
		Row(
			modifier = Modifier.padding(horizontal = 7.dp),
			horizontalArrangement = Arrangement.spacedBy(5.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			PensumCanvasLegendMarker(item = item, isSelected = isSelected)
			Text(
				text = stringResource(item.label),
				style = MaterialTheme.typography.labelSmall,
				fontWeight = FontWeight.SemiBold,
				color = TextPrimary.copy(alpha = if (isSelected) 0.96f else 0.86f),
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}
	}
}

@Composable
private fun PensumCanvasLegendMarker(
	item: LegendItem,
	isSelected: Boolean
) {
	PensumStatusIconMarker(
		imageVector = item.icon.imageVector,
		tint = item.color,
		hasBuiltInContainer = item.icon.hasBuiltInContainer,
		markerSize = 15.dp,
		iconSize = 11.dp,
		borderWidth = 1.2.dp,
		backgroundColor = if (isSelected) item.color.copy(alpha = 0.18f) else PanelBackground
	)
}

@Composable
private fun PensumCanvasLegendClearButton(
	onClick: () -> Unit
) {
	Surface(
		modifier = Modifier
			.size(28.dp)
			.selectable(
				selected = false,
				role = Role.Button,
				onClick = onClick
			)
			.testTag(PensumUiTags.StatusFilterClear),
		shape = CircleShape,
		color = PanelBackground,
		border = BorderStroke(1.dp, PanelBorder.copy(alpha = 0.82f))
	) {
		Box(contentAlignment = Alignment.Center) {
			Icon(
				imageVector = Icons.Outlined.Close,
				contentDescription = stringResource(Res.string.pensum_canvas_filters_clear),
				tint = TextPrimary,
				modifier = Modifier.size(15.dp)
			)
		}
	}
}

private data class LegendItem(
	val statusType: PensumNodeStatusType,
	val label: StringResource,
	val color: Color,
	val icon: PensumStatusIconVisual
)
