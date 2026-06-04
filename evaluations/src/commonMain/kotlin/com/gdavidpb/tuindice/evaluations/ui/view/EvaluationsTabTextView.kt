package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun EvaluationsTabTextView(
	modifier: Modifier = Modifier,
	text: String,
	selected: Boolean,
	onClick: () -> Unit
) {
	Column(
		modifier = modifier.clickable(onClick = onClick),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text(
			modifier = Modifier.padding(vertical = 8.dp),
			text = text,
			color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
			style = MaterialTheme.typography.titleMedium,
			fontWeight = FontWeight.Bold
		)

		Box(
			modifier = Modifier
				.width(72.dp)
				.height(3.dp)
				.background(
					color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
					shape = RoundedCornerShape(2.dp)
				)
		)
	}
}
