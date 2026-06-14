package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusDisplay
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.pensum.ui.model.toStatusIconVisual
import com.gdavidpb.tuindice.pensum.ui.view.PensumStatusIconMarker
import com.gdavidpb.tuindice.pensum.ui.view.pensumGraphColors
import com.gdavidpb.tuindice.pensum.ui.view.toStatusColor
import org.jetbrains.compose.resources.stringResource

@Composable
fun PensumSubjectStatusBadge(
	status: PensumNodeStatusDisplay
) {
	val graphColors = pensumGraphColors()
	val statusColor = status.toStatusColor(graphColors)
	val statusIcon = status.toStatusIconVisual()
	Row(
		horizontalArrangement = Arrangement.spacedBy(6.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Box(
			modifier = Modifier.size(24.dp),
			contentAlignment = Alignment.Center
		) {
			PensumStatusIconMarker(
				imageVector = statusIcon.imageVector,
				tint = statusColor,
				hasBuiltInContainer = statusIcon.hasBuiltInContainer,
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
