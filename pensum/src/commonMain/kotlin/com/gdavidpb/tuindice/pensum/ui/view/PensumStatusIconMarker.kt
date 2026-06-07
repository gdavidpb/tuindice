package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp

@Composable
fun PensumStatusIconMarker(
	imageVector: ImageVector,
	tint: Color,
	hasBuiltInContainer: Boolean,
	markerSize: Dp,
	iconSize: Dp,
	borderWidth: Dp,
	modifier: Modifier = Modifier,
	backgroundColor: Color = PanelBackground
) {
	Box(
		modifier = modifier.size(markerSize),
		contentAlignment = Alignment.Center
	) {
		if (hasBuiltInContainer) {
			Icon(
				imageVector = imageVector,
				contentDescription = null,
				tint = tint,
				modifier = Modifier.fillMaxSize()
			)
		} else {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(backgroundColor, CircleShape)
					.border(borderWidth, tint, CircleShape),
				contentAlignment = Alignment.Center
			) {
				Icon(
					imageVector = imageVector,
					contentDescription = null,
					tint = tint,
					modifier = Modifier.size(iconSize)
				)
			}
		}
	}
}
