package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.presentation.model.PensumSubjectRelationItem
import com.gdavidpb.tuindice.pensum.ui.model.PensumSubjectDetailNavigationDirection
import com.gdavidpb.tuindice.pensum.ui.model.PensumSubjectDetailNavigationTarget

@Composable
fun PensumSubjectRouteColumn(
	title: String,
	items: List<PensumSubjectRelationItem>,
	testTag: String,
	rowTag: (String) -> String,
	originNodeId: String,
	navigationDirection: PensumSubjectDetailNavigationDirection,
	onRelatedSubjectClick: (PensumSubjectDetailNavigationTarget) -> Unit,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
			.testTag(testTag),
		verticalArrangement = Arrangement.spacedBy(6.dp),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text(
			text = title,
			style = MaterialTheme.typography.labelSmall,
			fontWeight = FontWeight.SemiBold,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)
		items.forEach { item ->
			PensumRouteRelationCard(
				item = item,
				testTag = rowTag(item.nodeId),
				onClick = {
					onRelatedSubjectClick(
						PensumSubjectDetailNavigationTarget(
							nodeId = item.nodeId,
							originNodeId = originNodeId,
							direction = navigationDirection
						)
					)
				}
			)
		}
	}
}
