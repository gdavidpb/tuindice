package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.presentation.model.PensumSubjectRelationItem
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.pensum.ui.model.PensumSubjectDetailNavigationDirection
import com.gdavidpb.tuindice.pensum.ui.model.PensumSubjectDetailNavigationTarget
import org.jetbrains.compose.resources.stringResource
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_subject_detail_also_with

@Composable
fun PensumSubjectCorequisiteSection(
	originNodeId: String,
	items: List<PensumSubjectRelationItem>,
	onRelatedSubjectClick: (PensumSubjectDetailNavigationTarget) -> Unit
) {
	if (items.isEmpty()) return

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.testTag(PensumUiTags.SubjectDetailCorequisites),
		verticalArrangement = Arrangement.spacedBy(8.dp)
	) {
		Text(
			modifier = Modifier.testTag(PensumUiTags.SubjectDetailAlsoWith),
			text = stringResource(Res.string.pensum_subject_detail_also_with),
			style = MaterialTheme.typography.labelLarge,
			fontWeight = FontWeight.Bold,
			color = MaterialTheme.colorScheme.onSurface
		)
		items.forEach { item ->
			PensumSubjectRelationRow(
				item = item,
				testTag = PensumUiTags.subjectDetailCorequisite(item.nodeId),
				statusTestTag = PensumUiTags.subjectDetailRelationStatus(item.nodeId),
				onClick = {
					onRelatedSubjectClick(
						PensumSubjectDetailNavigationTarget(
							nodeId = item.nodeId,
							originNodeId = originNodeId,
							direction = PensumSubjectDetailNavigationDirection.Lateral
						)
					)
				}
			)
		}
	}
}
