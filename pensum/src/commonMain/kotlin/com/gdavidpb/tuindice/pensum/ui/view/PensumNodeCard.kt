package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_subject_stats_content_description

@Composable
fun PensumNodeCard(
	node: PensumScreenModel.Node,
	isSelected: Boolean,
	isRequirementHighlighted: Boolean,
	onSubjectStatsClick: (subjectCode: String) -> Unit,
	modifier: Modifier = Modifier
) {
	val colors = node.visualStyle.toNodeColors()
	val isHighlighted = isSelected || isRequirementHighlighted
	val subjectCode = node.subjectCode
	val chipItems = remember(node.displayCodes, colors.chip, colors.chipText) {
		node.displayCodes.map { code ->
			code to code.toPensumChipColors(
				fallbackContainer = colors.chip,
				fallbackContent = colors.chipText
			)
		}
	}
	Surface(
		modifier = modifier,
		shape = RoundedCornerShape(8.dp),
		color = colors.container,
		border = BorderStroke(
			width = if (isHighlighted) 2.2.dp else 1.2.dp,
			color = if (isHighlighted) Selected else colors.border
		),
		shadowElevation = if (isSelected || node.isCurrent) 8.dp else 0.dp
	) {
		Box(modifier = Modifier.fillMaxSize().padding(10.dp)) {
			Column(
				modifier = Modifier.align(Alignment.TopStart)
			) {
				Row(
					modifier = Modifier.padding(end = if (node.isApproved) 28.dp else 0.dp),
					horizontalArrangement = Arrangement.spacedBy(4.dp)
				) {
					chipItems.forEach { (code, chipColors) ->
						Text(
							modifier = Modifier
								.widthIn(max = if (chipItems.size > 1) 54.dp else 112.dp)
								.background(chipColors.container, RoundedCornerShape(6.dp))
								.padding(horizontal = 6.dp, vertical = 4.dp),
							text = code,
							style = MaterialTheme.typography.labelMedium,
							fontWeight = FontWeight.SemiBold,
							color = chipColors.content,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis
						)
					}
				}
				Spacer(modifier = Modifier.height(10.dp))
				Text(
					text = node.name,
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = if (node.isCurrent) FontWeight.Bold else FontWeight.Medium,
					color = colors.text,
					maxLines = 2,
					overflow = TextOverflow.Ellipsis
				)
				Spacer(modifier = Modifier.height(8.dp))
				Text(
					text = "${node.credits} UC",
					style = MaterialTheme.typography.bodyMedium,
					color = colors.secondaryText
				)
			}
			if (node.isApproved) {
				Box(
					modifier = Modifier
						.align(Alignment.TopEnd)
						.size(22.dp)
						.background(PanelBackground, CircleShape)
						.border(1.4.dp, Approved, CircleShape),
					contentAlignment = Alignment.Center
				) {
					Icon(
						imageVector = Icons.Filled.Check,
						contentDescription = null,
						tint = Approved,
						modifier = Modifier.size(16.dp)
					)
				}
			}
			if (node.hasSubjectStatsAction && subjectCode != null) {
				IconButton(
					modifier = Modifier
						.align(Alignment.BottomEnd)
						.size(32.dp)
						.testTag(PensumUiTags.nodeSubjectStatsButton(node.id)),
					onClick = { onSubjectStatsClick(subjectCode) }
				) {
					Icon(
						imageVector = Icons.Outlined.BarChart,
						contentDescription = stringResource(
							Res.string.pensum_subject_stats_content_description,
							subjectCode
						),
						tint = colors.secondaryText,
						modifier = Modifier.size(20.dp)
					)
				}
			}
		}
	}
}
