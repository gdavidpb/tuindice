package com.gdavidpb.tuindice.base.ui.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import com.gdavidpb.tuindice.base.R

@Composable
fun ConfirmationDialogEntry(
	icon: ImageVector,
	iconColor: Color = MaterialTheme.colorScheme.outline,
	text: String,
	textColor: Color = Color.Unspecified,
	onClick: () -> Unit
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onClick() }
	) {
		Row(
			modifier = Modifier
				.padding(
					horizontal = dimensionResource(id = R.dimen.dp_8),
					vertical = dimensionResource(id = R.dimen.dp_12)
				),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.Start
		) {
			Image(
				imageVector = icon,
				colorFilter = iconColor.let(ColorFilter::tint),
				contentDescription = null,
				modifier = Modifier
					.size(dimensionResource(id = R.dimen.dp_24))
			)

			Text(
				modifier = Modifier
					.padding(
						horizontal = dimensionResource(id = R.dimen.dp_24)
					)
					.fillMaxWidth(),
				text = text,
				color = textColor
			)
		}
	}
}