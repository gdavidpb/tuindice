package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusDisplay
import com.gdavidpb.tuindice.pensum.ui.view.pensumGraphColors
import com.gdavidpb.tuindice.pensum.ui.view.toStatusColor
import org.jetbrains.compose.resources.stringResource

@Composable
fun PensumSubjectRelationStatusBadge(
	status: PensumNodeStatusDisplay,
	modifier: Modifier = Modifier
) {
	val graphColors = pensumGraphColors()
	val statusColor = status.toStatusColor(graphColors)
	Row(
		modifier = modifier,
		horizontalArrangement = Arrangement.spacedBy(5.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		PensumSubjectRelationStatusMarker(status = status)
		Text(
			text = stringResource(status.labelResource()),
			style = MaterialTheme.typography.labelSmall,
			fontWeight = FontWeight.SemiBold,
			color = statusColor,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)
	}
}
