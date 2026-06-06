package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.pensum.ui.model.pensumTermOrdinalLabel
import com.gdavidpb.tuindice.pensum.ui.view.Approved
import com.gdavidpb.tuindice.pensum.ui.view.Available
import com.gdavidpb.tuindice.pensum.ui.view.CanvasNeutral
import com.gdavidpb.tuindice.pensum.ui.view.Current
import com.gdavidpb.tuindice.pensum.ui.view.PanelBackground
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_canvas_legend_approved
import tuindice.pensum.generated.resources.pensum_canvas_legend_available
import tuindice.pensum.generated.resources.pensum_canvas_legend_blocked
import tuindice.pensum.generated.resources.pensum_canvas_legend_current
import tuindice.pensum.generated.resources.pensum_subject_detail_close
import tuindice.pensum.generated.resources.pensum_subject_detail_credits
import tuindice.pensum.generated.resources.pensum_subject_detail_credits_value
import tuindice.pensum.generated.resources.pensum_subject_detail_fulfilled_by
import tuindice.pensum.generated.resources.pensum_subject_detail_no_term
import tuindice.pensum.generated.resources.pensum_subject_detail_stats
import tuindice.pensum.generated.resources.pensum_subject_detail_stats_unavailable
import tuindice.pensum.generated.resources.pensum_subject_detail_term
import tuindice.pensum.generated.resources.pensum_subject_detail_title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PensumSubjectDetailBottomSheet(
	model: PensumScreenModel,
	node: PensumScreenModel.Node,
	onSubjectStatsClick: (subjectCode: String) -> Unit,
	onDismissRequest: () -> Unit
) {
	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
	val status = node.toSubjectStatus()
	val termIndex = model.terms.indexOfFirst { term -> term.id == node.termId }
	val selectedTerm = model.terms
		.firstOrNull { term -> term.id == node.termId }
	val termLabel = if (termIndex >= 0) {
		pensumTermOrdinalLabel(
			number = termIndex + 1,
			shouldIncludeText = true
		)
	} else {
		selectedTerm?.label
			?.takeIf(String::isNotBlank)
			?: stringResource(Res.string.pensum_subject_detail_no_term)
	}
	val statsCode = node.subjectStatsCode.takeIf { node.hasSubjectStatsAction }

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
				.testTag(PensumUiTags.SubjectDetailSheet),
			verticalArrangement = Arrangement.spacedBy(14.dp)
		) {
			Surface(
				modifier = Modifier.fillMaxWidth(),
				shape = RoundedCornerShape(12.dp),
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
					Row(
						horizontalArrangement = Arrangement.spacedBy(10.dp),
						verticalAlignment = Alignment.Top
					) {
						PensumSubjectStatusBadge(status = status)
						Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
							Text(
								modifier = Modifier.testTag(PensumUiTags.SubjectDetailCode),
								text = node.displayCode,
								style = MaterialTheme.typography.titleSmall,
								fontWeight = FontWeight.Black,
								maxLines = 1,
								overflow = TextOverflow.Ellipsis
							)
							Text(
								modifier = Modifier.testTag(PensumUiTags.SubjectDetailName),
								text = node.name,
								style = MaterialTheme.typography.bodyMedium,
								fontWeight = FontWeight.SemiBold,
								maxLines = 2,
								overflow = TextOverflow.Ellipsis
							)
						}
					}

					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.spacedBy(8.dp)
					) {
						PensumSubjectDetailMeta(
							modifier = Modifier.weight(1f),
							label = stringResource(Res.string.pensum_subject_detail_term),
							value = termLabel,
							valueTag = PensumUiTags.SubjectDetailTermValue
						)
						PensumSubjectDetailMeta(
							modifier = Modifier.weight(1f),
							label = stringResource(Res.string.pensum_subject_detail_credits),
							value = stringResource(
								Res.string.pensum_subject_detail_credits_value,
								node.credits
							)
						)
					}
				}
			}

			if (node.fulfilledSubject != null) {
				PensumFulfilledSubjectSummary(fulfilledSubject = node.fulfilledSubject)
			}

			if (statsCode == null) {
				Text(
					modifier = Modifier.testTag(PensumUiTags.SubjectDetailStatsUnavailable),
					text = stringResource(Res.string.pensum_subject_detail_stats_unavailable),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
	}
}

@Composable
private fun PensumSubjectStatusBadge(
	status: SubjectStatus
) {
	Row(
		horizontalArrangement = Arrangement.spacedBy(6.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Box(
			modifier = Modifier
				.size(24.dp)
				.background(PanelBackground, CircleShape)
				.border(1.4.dp, status.color, CircleShape),
			contentAlignment = Alignment.Center
		) {
			Icon(
				imageVector = status.icon,
				contentDescription = null,
				tint = status.color,
				modifier = Modifier.size(16.dp)
			)
		}
		Text(
			modifier = Modifier.testTag(PensumUiTags.SubjectDetailStatus),
			text = stringResource(status.label),
			style = MaterialTheme.typography.labelMedium,
			fontWeight = FontWeight.SemiBold,
			color = status.color,
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
		shape = RoundedCornerShape(8.dp),
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
	fulfilledSubject: PensumScreenModel.FulfilledSubject
) {
	Surface(
		modifier = Modifier.fillMaxWidth(),
		shape = RoundedCornerShape(10.dp),
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
			Text(
				text = fulfilledSubject.code,
				style = MaterialTheme.typography.labelMedium,
				fontWeight = FontWeight.Black
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

private data class SubjectStatus(
	val label: StringResource,
	val color: Color,
	val icon: ImageVector
)

private fun PensumScreenModel.Node.toSubjectStatus(): SubjectStatus {
	return when {
		isApproved -> SubjectStatus(
			label = Res.string.pensum_canvas_legend_approved,
			color = Approved,
			icon = Icons.Filled.Check
		)
		isCurrent -> SubjectStatus(
			label = Res.string.pensum_canvas_legend_current,
			color = Current,
			icon = Icons.Filled.PlayArrow
		)
		isBlocked -> SubjectStatus(
			label = Res.string.pensum_canvas_legend_blocked,
			color = CanvasNeutral.copy(alpha = 0.72f),
			icon = Icons.Outlined.Lock
		)
		else -> SubjectStatus(
			label = Res.string.pensum_canvas_legend_available,
			color = Available,
			icon = Icons.Outlined.Add
		)
	}
}
