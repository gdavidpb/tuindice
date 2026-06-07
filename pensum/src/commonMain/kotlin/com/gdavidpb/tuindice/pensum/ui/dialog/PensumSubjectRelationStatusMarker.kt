package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusDisplay
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusType
import com.gdavidpb.tuindice.pensum.ui.model.toImageVector
import com.gdavidpb.tuindice.pensum.ui.view.PensumStatusIconMarker

@Composable
fun PensumSubjectRelationStatusMarker(
	status: PensumNodeStatusDisplay
) {
	val statusColor = Color(status.colorArgb)
	Box(
		modifier = Modifier.size(20.dp),
		contentAlignment = Alignment.Center
	) {
		PensumStatusIconMarker(
			imageVector = status.icon.toImageVector(),
			tint = statusColor,
			hasBuiltInContainer = status.type == PensumNodeStatusType.CURRENT,
			markerSize = 20.dp,
			iconSize = 13.dp,
			borderWidth = 1.2.dp
		)
	}
}
