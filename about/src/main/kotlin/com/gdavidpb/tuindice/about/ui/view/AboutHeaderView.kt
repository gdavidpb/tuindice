package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.about.R

@Composable
fun AboutHeaderView(
	text: String,
	content: @Composable () -> Unit
) {
	Text(
		text = text,
		modifier = Modifier
			.padding(
				horizontal = dimensionResource(id = R.dimen.dp_24),
				vertical = dimensionResource(id = R.dimen.dp_16)
			)
			.fillMaxWidth(),
		style = MaterialTheme.typography.bodyLarge,
		color = MaterialTheme.colorScheme.onSurface,
		fontWeight = FontWeight.Medium
	)

	content()
}