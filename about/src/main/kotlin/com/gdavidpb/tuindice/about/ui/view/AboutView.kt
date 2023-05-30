package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import com.gdavidpb.tuindice.about.R

@Composable
fun AboutView(
	icon: ImageVector,
	text: String,
	tint: Color? = null,
	size: Dp = dimensionResource(id = R.dimen.dp_18),
	onClick: () -> Unit = {}
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onClick() }
	) {
		Row(
			modifier = Modifier
				.padding(
					horizontal = dimensionResource(id = R.dimen.dp_16),
					vertical = dimensionResource(id = R.dimen.dp_12)
				),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.Start
		) {
			Image(
				imageVector = icon,
				colorFilter = tint?.let(ColorFilter::tint),
				contentDescription = text.substringBefore('\n'),
				modifier = Modifier
					.size(size)
			)

			AboutSpanText(text = text)
		}
	}
}