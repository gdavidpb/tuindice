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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.BaseUiTags

@Composable
fun ConfirmationDialogEntry(
	icon: ImageVector,
	iconColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
	text: String,
	textColor: Color = Color.Unspecified,
	onClick: () -> Unit
) {
	Box(
		modifier = Modifier
			.testTag(BaseUiTags.ConfirmationDialogEntry)
			.fillMaxWidth()
			.clickable { onClick() }
	) {
		Row(
			modifier = Modifier
				.padding(
					horizontal = 8.dp,
					vertical = 12.dp
				),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.Start
		) {
			Image(
				imageVector = icon,
				colorFilter = iconColor.let(ColorFilter::tint),
				contentDescription = null,
				modifier = Modifier
					.size(24.dp)
			)

			Text(
				modifier = Modifier
					.padding(horizontal = 24.dp)
					.fillMaxWidth(),
				text = text,
				color = textColor
			)
		}
	}
}
