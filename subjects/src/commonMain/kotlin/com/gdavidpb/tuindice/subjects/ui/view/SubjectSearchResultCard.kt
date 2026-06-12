package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
			IconButton(
				modifier = Modifier
					.size(36.dp)
					.testTag(SubjectsUiTags.searchResultStatsButton(item.subjectCode)),
				onClick = onClick
			) {
				Icon(
					modifier = Modifier.size(20.dp),
					imageVector = Icons.Outlined.BarChart,
					contentDescription = stringResource(
						Res.string.subjects_search_result_content_description,
						item.subjectCode
					),
					tint = MaterialTheme.colorScheme.onSurfaceVariant
				)
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
		Icon(
			modifier = Modifier.size(16.dp),
			imageVector = visual.icon,
			contentDescription = null,
			tint = visual.color
		)
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
			icon = Icons.Filled.Check,
			color = AcademicStatusColors.approved()
		)

		AcademicPensumNodeStatus.CURRENT -> SubjectSearchStatusVisual(
			text = stringResource(Res.string.subjects_search_status_current),
			icon = Icons.Outlined.RadioButtonChecked,
			color = MaterialTheme.colorScheme.primary
		)

		AcademicPensumNodeStatus.AVAILABLE -> SubjectSearchStatusVisual(
			text = stringResource(Res.string.subjects_search_status_available),
			icon = Icons.Outlined.Add,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)

		AcademicPensumNodeStatus.BLOCKED -> SubjectSearchStatusVisual(
			text = stringResource(Res.string.subjects_search_status_blocked),
			icon = Icons.Outlined.Lock,
			color = AcademicStatusColors.blocked()
		)
	}
}

private data class SubjectSearchStatusVisual(
	val text: String,
	val icon: ImageVector,
	val color: Color
)
