package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeItem
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.pensum.ui.view.PensumElementShape
import org.jetbrains.compose.resources.stringResource
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_subject_detail_route_selected

@Composable
fun PensumSelectedSubjectRouteCard(
	node: PensumNodeItem,
	modifier: Modifier = Modifier
) {
	val detail = node.detail
	val borderColor = Color(node.visualStyle.borderArgb)

	Column(
		modifier = modifier,
		verticalArrangement = Arrangement.spacedBy(6.dp),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text(
			text = stringResource(Res.string.pensum_subject_detail_route_selected),
			style = MaterialTheme.typography.labelSmall,
			fontWeight = FontWeight.SemiBold,
			color = borderColor,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)
		Surface(
			modifier = Modifier
				.fillMaxWidth()
				.testTag(PensumUiTags.SubjectDetailSelectedRouteCard),
			shape = PensumElementShape,
			color = MaterialTheme.colorScheme.surface.copy(alpha = 0.62f),
			border = BorderStroke(
				width = 1.4.dp,
				color = borderColor
			)
		) {
			Column(
				modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
				verticalArrangement = Arrangement.spacedBy(7.dp)
			) {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					PensumStyledSubjectCodeChip(
						code = detail.code,
						visualStyle = node.visualStyle
					)
					PensumSubjectRelationStatusMarker(status = detail.status)
				}
				Text(
					text = detail.name,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
		}
	}
}
