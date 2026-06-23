package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubjectAvailability
import com.gdavidpb.tuindice.record.presentation.model.CreateTermSubjectItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.create_term_subject_already_planned
import tuindice.record.generated.resources.create_term_subject_approved
import tuindice.record.generated.resources.create_term_subject_current
import tuindice.record.generated.resources.create_term_subject_not_in_pensum
import tuindice.record.generated.resources.create_term_subject_requirement_pending
import tuindice.record.generated.resources.create_term_subject_tooltip_already_planned
import tuindice.record.generated.resources.create_term_subject_tooltip_approved
import tuindice.record.generated.resources.create_term_subject_tooltip_current
import tuindice.record.generated.resources.create_term_subject_tooltip_unavailable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTermSubjectStatusRow(
	subject: CreateTermSubjectItem,
	availableText: String,
	availableIcon: CreateTermSubjectStatusIcon
) {
	val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
	val tooltipText = subject.tooltipText()
	val status = subject.status(
		availableIcon = availableIcon,
		copy = SubjectStatusCopy(
			availableText = availableText,
			approvedText = stringResource(Res.string.create_term_subject_approved),
			currentText = stringResource(Res.string.create_term_subject_current),
			alreadyPlannedText = stringResource(Res.string.create_term_subject_already_planned),
			notInPensumText = stringResource(Res.string.create_term_subject_not_in_pensum),
			blockedText = stringResource(Res.string.create_term_subject_requirement_pending)
		),
		palette = SubjectStatusPalette(
			availableColor = AcademicStatusColors.available(),
			approvedColor = AcademicStatusColors.approved(),
			blockedColor = AcademicStatusColors.blocked(),
			onSurfaceVariantColor = onSurfaceVariantColor
		)
	)
	Row(
		horizontalArrangement = Arrangement.spacedBy(6.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		CreateTermSubjectStatusMarker(status = status)
		if (tooltipText == null) {
			CreateTermSubjectStatusLabel(
				subject = subject,
				status = status
			)
		} else {
			val tooltipState = rememberTooltipState(isPersistent = true)
			val tooltipScope = rememberCoroutineScope()

			TooltipBox(
				positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
					TooltipAnchorPosition.Above
				),
				tooltip = {
					PlainTooltip(
						caretShape = TooltipDefaults.caretShape()
					) {
						Text(
							modifier = Modifier.testTag(
								RecordUiTags.createSyntheticTermSubjectStatusTooltip(
									subjectCode = subject.subjectCode
								)
							),
							text = tooltipText
						)
					}
				},
				state = tooltipState,
				onDismissRequest = tooltipState::dismiss,
				enableUserInput = false
			) {
				CreateTermSubjectStatusLabel(
					modifier = Modifier.clickable {
						if (tooltipState.isVisible) {
							tooltipState.dismiss()
						} else {
							tooltipScope.launch {
								tooltipState.show()
							}
						}
					},
					subject = subject,
					status = status
				)
			}
		}
	}
}

@Composable
private fun CreateTermSubjectItem.tooltipText(): String? {
	return when (availability) {
		SyntheticTermSubjectAvailability.APPROVED ->
			availabilityDetail?.termLabel?.let { termLabel ->
				stringResource(Res.string.create_term_subject_tooltip_approved, termLabel)
			}

		SyntheticTermSubjectAvailability.CURRENT ->
			availabilityDetail?.termLabel?.let { termLabel ->
				stringResource(Res.string.create_term_subject_tooltip_current, termLabel)
			}

		SyntheticTermSubjectAvailability.ALREADY_PLANNED ->
			availabilityDetail?.termLabel?.let { termLabel ->
				stringResource(Res.string.create_term_subject_tooltip_already_planned, termLabel)
			}

		SyntheticTermSubjectAvailability.BLOCKED ->
			availabilityDetail
				?.missingSubjectCodes
				?.takeIf { subjectCodes -> subjectCodes.isNotEmpty() }
				?.joinToString(separator = ", ")
				?.let { missingSubjectCodes ->
					stringResource(Res.string.create_term_subject_tooltip_unavailable, missingSubjectCodes)
				}

		else -> null
	}
}

@Composable
private fun CreateTermSubjectStatusLabel(
	subject: CreateTermSubjectItem,
	status: SubjectStatus,
	modifier: Modifier = Modifier
) {
	Text(
		modifier = modifier.testTag(
			RecordUiTags.createSyntheticTermSubjectStatus(
				subjectCode = subject.subjectCode,
				status = subject.availability.name.lowercase()
			)
		),
		text = status.text,
		style = MaterialTheme.typography.bodySmall,
		color = status.color,
		maxLines = 1,
		overflow = TextOverflow.Ellipsis
	)
}

private fun CreateTermSubjectItem.status(
	availableIcon: CreateTermSubjectStatusIcon,
	copy: SubjectStatusCopy,
	palette: SubjectStatusPalette
): SubjectStatus {
	return when (availability) {
		SyntheticTermSubjectAvailability.APPROVED ->
			SubjectStatus(
				text = copy.approvedText,
				color = palette.approvedColor,
				icon = CreateTermSubjectStatusIcon.Check
			)

		SyntheticTermSubjectAvailability.CURRENT ->
			SubjectStatus(
				text = copy.currentText,
				color = palette.availableColor,
				icon = CreateTermSubjectStatusIcon.Current
			)

		SyntheticTermSubjectAvailability.AVAILABLE ->
			SubjectStatus(
				text = copy.availableText,
				color = palette.availableColor,
				icon = availableIcon
			)

		SyntheticTermSubjectAvailability.NOT_IN_PENSUM ->
			SubjectStatus(
				text = copy.notInPensumText,
				color = palette.onSurfaceVariantColor,
				icon = CreateTermSubjectStatusIcon.OutsidePensum
			)

		SyntheticTermSubjectAvailability.ALREADY_PLANNED ->
			SubjectStatus(
				text = copy.alreadyPlannedText,
				color = palette.onSurfaceVariantColor,
				icon = CreateTermSubjectStatusIcon.Planned
			)

		SyntheticTermSubjectAvailability.BLOCKED ->
			SubjectStatus(
				text = copy.blockedText,
				color = palette.blockedColor,
				icon = CreateTermSubjectStatusIcon.Blocked
			)
	}
}

@Composable
private fun CreateTermSubjectStatusMarker(status: SubjectStatus) {
	Box(
		modifier = Modifier
			.size(20.dp)
			.background(MaterialTheme.colorScheme.surfaceContainerLow, CircleShape)
			.border(1.2.dp, status.color, CircleShape),
		contentAlignment = Alignment.Center
	) {
		status.icon.imageVector()?.let { icon ->
			Icon(
				modifier = Modifier.size(13.dp),
				imageVector = icon,
				contentDescription = null,
				tint = status.color
			)
		} ?: Box(
			modifier = Modifier
				.size(6.dp)
				.background(status.color, CircleShape)
		)
	}
}

private fun CreateTermSubjectStatusIcon.imageVector(): ImageVector? {
	return when (this) {
		CreateTermSubjectStatusIcon.Dot -> null
		CreateTermSubjectStatusIcon.Check -> Icons.Filled.Check
		CreateTermSubjectStatusIcon.Current -> Icons.Outlined.Schedule
		CreateTermSubjectStatusIcon.Planned -> Icons.Outlined.Schedule
		CreateTermSubjectStatusIcon.OutsidePensum -> Icons.Outlined.Map
		CreateTermSubjectStatusIcon.Available -> Icons.Outlined.Add
		CreateTermSubjectStatusIcon.Blocked -> Icons.Outlined.Lock
	}
}

private data class SubjectStatus(
	val text: String,
	val color: Color,
	val icon: CreateTermSubjectStatusIcon
)

private data class SubjectStatusCopy(
    val availableText: String,
    val approvedText: String,
    val currentText: String,
    val alreadyPlannedText: String,
    val notInPensumText: String,
    val blockedText: String
)

private data class SubjectStatusPalette(
    val availableColor: Color,
    val approvedColor: Color,
    val blockedColor: Color,
    val onSurfaceVariantColor: Color
)
