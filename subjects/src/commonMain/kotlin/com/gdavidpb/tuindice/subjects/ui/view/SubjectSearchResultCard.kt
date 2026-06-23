package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.view.SubjectResultCard
import com.gdavidpb.tuindice.subjects.presentation.model.SubjectSearchResultItem
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.subjects.generated.resources.Res
import tuindice.subjects.generated.resources.subjects_search_result_content_description
import tuindice.subjects.generated.resources.subjects_search_status_approved
import tuindice.subjects.generated.resources.subjects_search_status_available
import tuindice.subjects.generated.resources.subjects_search_status_blocked
import tuindice.subjects.generated.resources.subjects_search_status_current

@Composable
fun SubjectSearchResultCard(
	item: SubjectSearchResultItem,
	onClick: () -> Unit
) {
	SubjectResultCard(
		subjectCode = item.subjectCode,
		nameText = item.name,
		creditsText = item.creditsText,
		containerTestTag = SubjectsUiTags.searchResult(item.subjectCode),
		onClick = onClick,
		statusContent = item.pensumStatus?.let { status ->
			{
				SubjectSearchResultStatusBadge(
					subjectCode = item.subjectCode,
					status = status,
					onClick = onClick
				)
			}
		},
		trailingContent = {
			Surface(
				shape = RoundedCornerShape(TuIndiceRadius.Medium),
				color = MaterialTheme.colorScheme.surface.copy(alpha = 0.48f),
				border = BorderStroke(
					width = 1.dp,
					color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = TuIndiceAlpha.Muted)
				)
			) {
				IconButton(
					modifier = Modifier
						.size(38.dp)
						.testTag(SubjectsUiTags.searchResultStatsButton(item.subjectCode)),
					onClick = onClick
				) {
					Icon(
						imageVector = Icons.Outlined.BarChart,
						contentDescription = stringResource(
							Res.string.subjects_search_result_content_description,
							item.subjectCode
						),
						tint = MaterialTheme.colorScheme.onSurface
					)
				}
			}
		}
	)
}

@Composable
private fun SubjectSearchResultStatusBadge(
	subjectCode: String,
	status: AcademicPensumNodeStatus,
	onClick: () -> Unit
) {
	val visual = status.visual()
	val interactionSource = remember { MutableInteractionSource() }
	Row(
		modifier = Modifier
			.testTag(
				SubjectsUiTags.searchResultStatus(
					subjectCode = subjectCode,
					status = status.name.lowercase()
				)
			)
			.clickable(
				interactionSource = interactionSource,
				indication = null,
				onClick = onClick
			),
		horizontalArrangement = Arrangement.spacedBy(6.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		SubjectSearchStatusMarker(visual = visual)
		Text(
			text = visual.text,
			style = MaterialTheme.typography.bodySmall,
			color = visual.color
		)
	}
}

@Composable
private fun AcademicPensumNodeStatus.visual(): SubjectSearchStatusVisual {
	return when (this) {
		AcademicPensumNodeStatus.APPROVED -> SubjectSearchStatusVisual(
			text = stringResource(Res.string.subjects_search_status_approved),
			icon = SubjectSearchStatusIcon.Check,
			color = AcademicStatusColors.approved()
		)

		AcademicPensumNodeStatus.CURRENT -> SubjectSearchStatusVisual(
			text = stringResource(Res.string.subjects_search_status_current),
			icon = SubjectSearchStatusIcon.Current,
			color = AcademicStatusColors.available()
		)

		AcademicPensumNodeStatus.AVAILABLE -> SubjectSearchStatusVisual(
			text = stringResource(Res.string.subjects_search_status_available),
			icon = SubjectSearchStatusIcon.Available,
			color = AcademicStatusColors.available()
		)

		AcademicPensumNodeStatus.BLOCKED -> SubjectSearchStatusVisual(
			text = stringResource(Res.string.subjects_search_status_blocked),
			icon = SubjectSearchStatusIcon.Blocked,
			color = AcademicStatusColors.blocked()
		)
	}
}

@Composable
private fun SubjectSearchStatusMarker(visual: SubjectSearchStatusVisual) {
	Box(
		modifier = Modifier
			.size(20.dp)
			.background(MaterialTheme.colorScheme.surfaceContainerLow, CircleShape)
			.border(1.2.dp, visual.color, CircleShape),
		contentAlignment = Alignment.Center
	) {
		visual.icon.imageVector()?.let { icon ->
			Icon(
				modifier = Modifier.size(13.dp),
				imageVector = icon,
				contentDescription = null,
				tint = visual.color
			)
		} ?: Box(
			modifier = Modifier
				.size(6.dp)
				.background(visual.color, CircleShape)
		)
	}
}

private fun SubjectSearchStatusIcon.imageVector(): ImageVector? {
	return when (this) {
		SubjectSearchStatusIcon.Check -> Icons.Filled.Check
		SubjectSearchStatusIcon.Current -> Icons.Outlined.Schedule
		SubjectSearchStatusIcon.Available -> Icons.Outlined.Add
		SubjectSearchStatusIcon.Blocked -> Icons.Outlined.Lock
	}
}

private data class SubjectSearchStatusVisual(
	val text: String,
	val icon: SubjectSearchStatusIcon,
	val color: Color
)

private enum class SubjectSearchStatusIcon {
    Check,
    Current,
    Available,
    Blocked
}
