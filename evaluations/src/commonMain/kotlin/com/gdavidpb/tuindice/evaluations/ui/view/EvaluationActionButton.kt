package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun EvaluationActionButton(
	modifier: Modifier = Modifier,
	text: String,
	containerColor: Color,
	contentColor: Color,
	shape: Shape,
	onClick: () -> Unit,
	icon: @Composable () -> Unit
) {
	Box(
		modifier = modifier
			.background(
				color = containerColor,
				shape = shape
			)
			.clickable(onClick = onClick)
			.fillMaxHeight()
			.padding(horizontal = 10.dp),
		contentAlignment = Alignment.Center
	) {
		Row(
			horizontalArrangement = Arrangement.Center,
			verticalAlignment = Alignment.CenterVertically
		) {
			CompositionLocalProvider(LocalContentColor provides contentColor) {
				icon()
			}

			Text(
				modifier = Modifier.padding(start = 4.dp),
				text = text,
				color = contentColor,
				style = MaterialTheme.typography.labelMedium,
				fontWeight = FontWeight.SemiBold,
				maxLines = 1,
				overflow = TextOverflow.Clip
			)
		}
	}
}
