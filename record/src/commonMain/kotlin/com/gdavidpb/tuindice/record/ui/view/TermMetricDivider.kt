package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TermMetricDivider() {
	Box(
		modifier = Modifier
			.padding(vertical = 8.dp)
			.fillMaxHeight()
			.width(1.dp)
			.background(MaterialTheme.colorScheme.outlineVariant)
	)
}
