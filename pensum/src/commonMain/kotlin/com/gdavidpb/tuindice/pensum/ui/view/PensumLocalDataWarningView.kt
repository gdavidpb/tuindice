package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags

@Composable
fun PensumLocalDataWarningView(
	message: String,
	modifier: Modifier = Modifier
) {
	val graphColors = pensumGraphColors()
	Row(
		modifier = modifier
			.testTag(PensumUiTags.LocalDataWarning)
			.background(graphColors.panelBackground, PensumElementShape)
			.border(1.dp, graphColors.canvasNeutral.copy(alpha = 0.42f), PensumElementShape)
			.padding(horizontal = 12.dp, vertical = 8.dp),
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			modifier = Modifier.size(16.dp),
			imageVector = Icons.Outlined.Info,
			contentDescription = null,
			tint = graphColors.textSecondary
		)
		Text(
			text = message,
			color = graphColors.textPrimary,
			style = MaterialTheme.typography.labelMedium
		)
	}
}
