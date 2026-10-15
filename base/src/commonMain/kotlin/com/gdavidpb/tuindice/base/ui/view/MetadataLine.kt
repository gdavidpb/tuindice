package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

/**
 * One line of detail under the title of a row: its [icon], drawn at 16 dp and centered in the 18 dp slot
 * that the icon of the title takes, and its [text], written where the name of the title starts. The text
 * wraps when it must, and nothing is cut.
 */
@Composable
fun MetadataLine(
	modifier: Modifier = Modifier,
	icon: ImageVector,
	text: String,
	color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
	Row(
		modifier = modifier,
		verticalAlignment = Alignment.CenterVertically
	) {
		Box(
			modifier = Modifier.width(TitleIconSlot),
			contentAlignment = Alignment.Center
		) {
			Icon(
				modifier = Modifier.size(MetadataIconSize),
				imageVector = icon,
				tint = color,
				contentDescription = null
			)
		}

		Text(
			modifier = Modifier.padding(start = TuIndiceSpacing.Medium),
			text = text,
			color = color,
			style = MaterialTheme.typography.bodyMedium
		)
	}
}

// The title's icon is 18 dp and its name starts 8 dp after it: the detail's icon is centered in that same 18 dp.
private val TitleIconSlot = 18.dp
private val MetadataIconSize = 16.dp
