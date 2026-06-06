package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
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
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeItem
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.pensum.ui.model.toImageVector
import org.jetbrains.compose.resources.stringResource
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_subject_detail_content_description

@Composable
fun PensumNodeCard(
	node: PensumNodeItem,
	isSelected: Boolean,
	isRequirementHighlighted: Boolean,
	isUnlockHighlighted: Boolean,
	onDetailClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	val colors = node.visualStyle.toNodeColors()
	val isHighlighted = isSelected || isRequirementHighlighted || isUnlockHighlighted
	val fulfilledSubject = node.fulfilledSubject
	val statusColor = Color(node.status.colorArgb)
	val chipColors = node.displayCode.toPensumChipColors(
		fallbackContainer = colors.chip,
		fallbackContent = colors.chipText
	)

	Surface(
		modifier = modifier,
		shape = PensumElementShape,
		color = colors.container,
		border = BorderStroke(
			width = if (isHighlighted) 2.2.dp else 1.2.dp,
			color = colors.border
		),
		shadowElevation = if (isSelected || node.isCurrent) 8.dp else 0.dp
	) {
		Box(modifier = Modifier.fillMaxSize().padding(10.dp)) {
			if (isSelected) {
				Box(
					modifier = Modifier
						.align(Alignment.TopStart)
						.size(1.dp)
						.testTag(PensumUiTags.focusedNode(node.id))
				)
			}
			Column(
				modifier = Modifier.align(Alignment.TopStart)
			) {
				Text(
					modifier = Modifier
						.padding(end = 28.dp)
						.background(chipColors.container, PensumElementShape)
						.padding(horizontal = 8.dp, vertical = 4.dp),
					text = node.displayCode,
					style = MaterialTheme.typography.labelMedium,
					fontWeight = FontWeight.SemiBold,
					color = chipColors.content,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
				Spacer(modifier = Modifier.height(10.dp))
				if (fulfilledSubject != null) {
					Text(
						text = fulfilledSubject.code,
						style = MaterialTheme.typography.labelMedium,
						fontWeight = FontWeight.SemiBold,
						color = colors.secondaryText,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis
					)
					Spacer(modifier = Modifier.height(4.dp))
				}
				Text(
					text = node.displayName,
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = if (node.isCurrent) FontWeight.Bold else FontWeight.Medium,
					color = colors.text,
					maxLines = 2,
					overflow = TextOverflow.Ellipsis
				)
				Spacer(modifier = Modifier.height(8.dp))
				Text(
					text = node.creditsText,
					style = MaterialTheme.typography.bodyMedium,
					color = colors.secondaryText
				)
			}
			Box(
				modifier = Modifier
					.align(Alignment.TopEnd)
					.size(22.dp)
					.background(PanelBackground, CircleShape)
					.border(1.4.dp, statusColor, CircleShape),
				contentAlignment = Alignment.Center
			) {
				Icon(
					imageVector = node.status.icon.toImageVector(),
					contentDescription = null,
					tint = statusColor,
					modifier = Modifier.size(16.dp)
				)
			}
			Box(
				modifier = Modifier
					.align(Alignment.BottomEnd)
					.size(22.dp)
					.clickable(onClick = onDetailClick)
					.testTag(PensumUiTags.nodeDetailButton(node.id)),
				contentAlignment = Alignment.Center
			) {
				Icon(
					imageVector = Icons.Outlined.Info,
					contentDescription = stringResource(
						Res.string.pensum_subject_detail_content_description,
						node.displayCode
					),
					tint = colors.secondaryText,
					modifier = Modifier.size(22.dp)
				)
			}
		}
	}
}
