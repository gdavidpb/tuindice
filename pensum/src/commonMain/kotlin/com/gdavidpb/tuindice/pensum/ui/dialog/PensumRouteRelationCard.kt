package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.gdavidpb.tuindice.pensum.ui.view.pensumGraphColors

@Composable
fun PensumRouteRelationCard(
	item: PensumSubjectRelationItem,
	testTag: String,
	onClick: () -> Unit
) {
	val graphColors = pensumGraphColors()

	Surface(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(
				role = Role.Button,
				onClick = onClick
			)
			.testTag(testTag),
		shape = PensumElementShape,
		color = graphColors.panelBackground,
		border = BorderStroke(
			width = 1.dp,
			color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
		)
	) {
		Column(
			modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
			verticalArrangement = Arrangement.spacedBy(5.dp)
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				PensumStyledSubjectCodeChip(
					code = item.code,
					visualStyle = item.visualStyle
				)
				Spacer(modifier = Modifier.weight(1f))
				PensumSubjectRelationStatusMarker(status = item.status)
				PensumOpenRelatedSubjectIcon(
					code = item.code,
					modifier = Modifier.padding(start = SubjectDetailOpenIconStartPadding)
				)
			}
			Text(
				text = item.name,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}
	}
}
