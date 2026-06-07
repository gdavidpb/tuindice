package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.pensum.presentation.model.PensumFulfilledSubjectItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusDisplay
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusType
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeVisualStyle
import com.gdavidpb.tuindice.pensum.presentation.model.PensumSubjectRelationItem
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.pensum.ui.model.toImageVector
import com.gdavidpb.tuindice.pensum.ui.view.Current
import com.gdavidpb.tuindice.pensum.ui.view.PensumElementShape
import com.gdavidpb.tuindice.pensum.ui.view.PensumStatusIconMarker
import com.gdavidpb.tuindice.pensum.ui.view.PensumSubjectCodeChip
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_canvas_legend_approved
import tuindice.pensum.generated.resources.pensum_canvas_legend_available
import tuindice.pensum.generated.resources.pensum_canvas_legend_blocked
import tuindice.pensum.generated.resources.pensum_canvas_legend_current
import tuindice.pensum.generated.resources.pensum_subject_detail_close
import tuindice.pensum.generated.resources.pensum_subject_detail_blocked_by
import tuindice.pensum.generated.resources.pensum_subject_detail_corequisites
import tuindice.pensum.generated.resources.pensum_subject_detail_credits
import tuindice.pensum.generated.resources.pensum_subject_detail_focus_subject
import tuindice.pensum.generated.resources.pensum_subject_detail_fulfilled_by
import tuindice.pensum.generated.resources.pensum_subject_detail_no_term
import tuindice.pensum.generated.resources.pensum_subject_detail_more
import tuindice.pensum.generated.resources.pensum_subject_detail_requirements
import tuindice.pensum.generated.resources.pensum_subject_detail_stats
import tuindice.pensum.generated.resources.pensum_subject_detail_stats_unavailable
import tuindice.pensum.generated.resources.pensum_subject_detail_term
import tuindice.pensum.generated.resources.pensum_subject_detail_title
import tuindice.pensum.generated.resources.pensum_subject_detail_unlocks

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PensumSubjectDetailBottomSheet(
	node: PensumNodeItem,
	onSubjectStatsClick: (subjectCode: String) -> Unit,
	onRelatedSubjectClick: (nodeId: String) -> Unit,
	onDismissRequest: () -> Unit
) {
	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
	val detail = node.detail
	val statsCode = detail.statsCode
	val hasMoreDetail =
		detail.fulfilledSubject != null ||
			detail.blockingReasons.isNotEmpty() ||
			detail.requirements.isNotEmpty() ||
			detail.corequisites.isNotEmpty() ||
			detail.unlocks.isNotEmpty()
	val isMoreDetailExpandedState = remember(node.id) {
		mutableStateOf(false)
	}

	LaunchedEffect(isMoreDetailExpandedState.value) {
		if (isMoreDetailExpandedState.value) {
			sheetState.expand()
		}
	}

	ConfirmationDialog(
		sheetState = sheetState,
		titleText = stringResource(Res.string.pensum_subject_detail_title),
		positiveText = statsCode?.let { stringResource(Res.string.pensum_subject_detail_stats) },
		negativeText = stringResource(Res.string.pensum_subject_detail_close),
		onPositiveClick = {
			if (statsCode != null) {
				onSubjectStatsClick(statsCode)
			}
		},
		onDismissRequest = onDismissRequest
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.verticalScroll(rememberScrollState())
				.testTag(PensumUiTags.SubjectDetailSheet),
			verticalArrangement = Arrangement.spacedBy(14.dp)
		) {
			PensumSubjectOverviewCard(node = node)

			if (statsCode == null) {
				Text(
					modifier = Modifier.testTag(PensumUiTags.SubjectDetailStatsUnavailable),
					text = stringResource(Res.string.pensum_subject_detail_stats_unavailable),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}

			if (!isMoreDetailExpandedState.value && hasMoreDetail) {
				PensumSubjectMoreDetailButton(
					onClick = { isMoreDetailExpandedState.value = true }
				)
			}

			if (isMoreDetailExpandedState.value) {
				if (detail.fulfilledSubject != null) {
					PensumFulfilledSubjectSummary(
						fulfilledSubject = detail.fulfilledSubject,
						visualStyle = node.visualStyle
					)
				}

				PensumSubjectRelationSection(
					title = stringResource(Res.string.pensum_subject_detail_blocked_by),
					items = detail.blockingReasons,
					testTag = PensumUiTags.SubjectDetailBlockingReasons,
					rowTag = { nodeId -> PensumUiTags.subjectDetailBlockingReason(nodeId) },
					onRelatedSubjectClick = onRelatedSubjectClick
				)
				PensumSubjectRelationSection(
					title = stringResource(Res.string.pensum_subject_detail_requirements),
					items = detail.requirements,
					testTag = PensumUiTags.SubjectDetailRequirements,
					rowTag = { nodeId -> PensumUiTags.subjectDetailRequirement(nodeId) },
					onRelatedSubjectClick = onRelatedSubjectClick
				)
				PensumSubjectRelationSection(
					title = stringResource(Res.string.pensum_subject_detail_corequisites),
					items = detail.corequisites,
					testTag = PensumUiTags.SubjectDetailCorequisites,
					rowTag = { nodeId -> PensumUiTags.subjectDetailCorequisite(nodeId) },
					onRelatedSubjectClick = onRelatedSubjectClick
				)
				PensumSubjectRelationSection(
					title = stringResource(Res.string.pensum_subject_detail_unlocks),
					items = detail.unlocks,
					testTag = PensumUiTags.SubjectDetailUnlocks,
					rowTag = { nodeId -> PensumUiTags.subjectDetailUnlock(nodeId) },
					onRelatedSubjectClick = onRelatedSubjectClick
				)
			}
		}
	}
}

@Composable
private fun PensumSubjectOverviewCard(
	node: PensumNodeItem
) {
	val detail = node.detail

	Surface(
		modifier = Modifier.fillMaxWidth(),
		shape = PensumElementShape,
		color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
		border = BorderStroke(
			width = 1.dp,
			color = MaterialTheme.colorScheme.outlineVariant
		)
	) {
		Column(
			modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Column(
				modifier = Modifier.fillMaxWidth(),
				verticalArrangement = Arrangement.spacedBy(10.dp)
			) {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					PensumStyledSubjectCodeChip(
						modifier = Modifier.testTag(PensumUiTags.SubjectDetailCode),
						code = detail.code,
						visualStyle = node.visualStyle
					)
					PensumSubjectStatusBadge(status = detail.status)
				}
				Text(
					modifier = Modifier.testTag(PensumUiTags.SubjectDetailName),
					text = detail.name,
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = FontWeight.SemiBold,
					maxLines = 2,
					overflow = TextOverflow.Ellipsis
				)
			}

			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(8.dp)
			) {
				PensumSubjectDetailMeta(
					modifier = Modifier.weight(1f),
					label = stringResource(Res.string.pensum_subject_detail_term),
					value = detail.termLabel ?: stringResource(Res.string.pensum_subject_detail_no_term),
					valueTag = PensumUiTags.SubjectDetailTermValue
				)
				PensumSubjectDetailMeta(
					modifier = Modifier.weight(1f),
					label = stringResource(Res.string.pensum_subject_detail_credits),
					value = detail.creditsText
				)
			}
		}
	}
}

@Composable
private fun PensumSubjectMoreDetailButton(
	onClick: () -> Unit
) {
	OutlinedButton(
		modifier = Modifier
			.fillMaxWidth()
			.testTag(PensumUiTags.SubjectDetailMoreButton),
		onClick = onClick
	) {
		Text(text = stringResource(Res.string.pensum_subject_detail_more))
		Icon(
			imageVector = Icons.Filled.KeyboardArrowDown,
			contentDescription = null,
			modifier = Modifier.size(18.dp)
		)
	}
}

@Composable
private fun PensumSubjectRelationSection(
	title: String,
	items: List<PensumSubjectRelationItem>,
	testTag: String,
	rowTag: (String) -> String,
	onRelatedSubjectClick: (nodeId: String) -> Unit
) {
	if (items.isEmpty()) return

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.testTag(testTag),
		verticalArrangement = Arrangement.spacedBy(8.dp)
	) {
		Text(
			text = title,
			style = MaterialTheme.typography.labelLarge,
			fontWeight = FontWeight.Black,
			color = MaterialTheme.colorScheme.onSurface
		)
		items.forEach { item ->
			PensumSubjectRelationRow(
				item = item,
				testTag = rowTag(item.nodeId),
				onClick = { onRelatedSubjectClick(item.nodeId) }
			)
		}
	}
}

@Composable
private fun PensumSubjectRelationRow(
	item: PensumSubjectRelationItem,
	testTag: String,
	onClick: () -> Unit
) {
	Surface(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(
				role = Role.Button,
				onClick = onClick
			)
			.testTag(testTag),
		shape = PensumElementShape,
		color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
		border = BorderStroke(
			width = 1.dp,
			color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
		)
	) {
		Row(
			modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
			horizontalArrangement = Arrangement.spacedBy(10.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Column(
				modifier = Modifier.weight(1f),
				verticalArrangement = Arrangement.spacedBy(6.dp)
			) {
				PensumStyledSubjectCodeChip(
					code = item.code,
					visualStyle = item.visualStyle
				)
				Text(
					text = item.name,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
			PensumSubjectRelationStatusBadge(status = item.status)
			Icon(
				imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
				contentDescription = stringResource(
					Res.string.pensum_subject_detail_focus_subject,
					item.code
				),
				tint = MaterialTheme.colorScheme.onSurfaceVariant,
				modifier = Modifier.size(18.dp)
			)
		}
	}
}

@Composable
private fun PensumSubjectRelationStatusBadge(
	status: PensumNodeStatusDisplay
) {
	val statusColor = Color(status.colorArgb)
	Row(
		horizontalArrangement = Arrangement.spacedBy(5.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Box(
			modifier = Modifier.size(20.dp),
			contentAlignment = Alignment.Center
		) {
			PensumStatusIconMarker(
				imageVector = status.icon.toImageVector(),
				tint = statusColor,
				hasBuiltInContainer = status.type == PensumNodeStatusType.CURRENT,
				markerSize = 20.dp,
				iconSize = 13.dp,
				borderWidth = 1.2.dp
			)
		}
		Text(
			text = stringResource(status.labelResource()),
			style = MaterialTheme.typography.labelSmall,
			fontWeight = FontWeight.SemiBold,
			color = statusColor,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)
	}
}

@Composable
private fun PensumSubjectStatusBadge(
	status: PensumNodeStatusDisplay
) {
	val statusColor = Color(status.colorArgb)
	Row(
		horizontalArrangement = Arrangement.spacedBy(6.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Box(
			modifier = Modifier.size(24.dp),
			contentAlignment = Alignment.Center
		) {
			PensumStatusIconMarker(
				imageVector = status.icon.toImageVector(),
				tint = statusColor,
				hasBuiltInContainer = status.type == PensumNodeStatusType.CURRENT,
				markerSize = 24.dp,
				iconSize = 16.dp,
				borderWidth = 1.4.dp
			)
		}
		Text(
			modifier = Modifier.testTag(PensumUiTags.SubjectDetailStatus),
			text = stringResource(status.labelResource()),
			style = MaterialTheme.typography.labelMedium,
			fontWeight = FontWeight.SemiBold,
			color = statusColor,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)
	}
}

@Composable
private fun PensumSubjectDetailMeta(
	label: String,
	value: String,
	modifier: Modifier = Modifier,
	valueTag: String? = null
) {
	Surface(
		modifier = modifier,
		shape = PensumElementShape,
		color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
		border = BorderStroke(
			width = 1.dp,
			color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
		)
	) {
		Column(
			modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
			verticalArrangement = Arrangement.spacedBy(2.dp)
		) {
			Text(
				text = label,
				style = MaterialTheme.typography.labelSmall,
				fontWeight = FontWeight.SemiBold,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
			Text(
				modifier = valueTag?.let { tag -> Modifier.testTag(tag) } ?: Modifier,
				text = value,
				style = MaterialTheme.typography.bodySmall,
				fontWeight = FontWeight.SemiBold,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}
	}
}

@Composable
private fun PensumFulfilledSubjectSummary(
	fulfilledSubject: PensumFulfilledSubjectItem,
	visualStyle: PensumNodeVisualStyle
) {
	Surface(
		modifier = Modifier.fillMaxWidth(),
		shape = PensumElementShape,
		color = Current.copy(alpha = 0.12f),
		border = BorderStroke(
			width = 1.dp,
			color = Current.copy(alpha = 0.34f)
		)
	) {
		Column(
			modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
			verticalArrangement = Arrangement.spacedBy(3.dp)
		) {
			Text(
				text = stringResource(Res.string.pensum_subject_detail_fulfilled_by),
				style = MaterialTheme.typography.labelSmall,
				fontWeight = FontWeight.SemiBold,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			PensumStyledSubjectCodeChip(
				code = fulfilledSubject.code,
				visualStyle = visualStyle
			)
			Text(
				text = fulfilledSubject.name,
				style = MaterialTheme.typography.bodySmall,
				maxLines = 2,
				overflow = TextOverflow.Ellipsis
			)
		}
	}
}

@Composable
private fun PensumStyledSubjectCodeChip(
	code: String,
	visualStyle: PensumNodeVisualStyle,
	modifier: Modifier = Modifier
) {
	PensumSubjectCodeChip(
		modifier = modifier,
		code = code,
		fallbackContainer = Color(visualStyle.chipArgb),
		fallbackContent = Color(visualStyle.chipTextArgb)
	)
}

private fun PensumNodeStatusDisplay.labelResource(): StringResource {
	return when {
		type == PensumNodeStatusType.APPROVED -> Res.string.pensum_canvas_legend_approved
		type == PensumNodeStatusType.CURRENT -> Res.string.pensum_canvas_legend_current
		type == PensumNodeStatusType.BLOCKED -> Res.string.pensum_canvas_legend_blocked
		else -> Res.string.pensum_canvas_legend_available
	}
}
