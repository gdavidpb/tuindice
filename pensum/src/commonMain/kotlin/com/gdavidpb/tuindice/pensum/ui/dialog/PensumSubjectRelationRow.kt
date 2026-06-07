package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.presentation.model.PensumSubjectRelationItem
import com.gdavidpb.tuindice.pensum.ui.view.PensumElementShape

@Composable
fun PensumSubjectRelationRow(
	item: PensumSubjectRelationItem,
	testTag: String,
	statusTestTag: String? = null,
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
			PensumSubjectRelationStatusBadge(
				modifier = statusTestTag?.let { tag -> Modifier.testTag(tag) } ?: Modifier,
				status = item.status
			)
			PensumOpenRelatedSubjectIcon(
				code = item.code,
				modifier = Modifier.padding(start = SubjectDetailOpenIconStartPadding)
			)
		}
	}
}
