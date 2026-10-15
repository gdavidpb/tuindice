package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

/**
 * The first line of a week row: the icon of what the row is and its title in capitals, and at the end the [trailing]
 * content, which is the chip of the state. The title takes the room the icon and the chip leave, and wraps instead of
 * being cut.
 */
@Composable
fun SummaryTitleLine(
	icon: ImageVector,
	iconTag: String,
	titleText: String,
	modifier: Modifier = Modifier,
	trailing: @Composable () -> Unit = {}
) {
	Row(
		modifier = modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium),
		verticalAlignment = Alignment.CenterVertically
	) {
		Row(
			modifier = Modifier.weight(1f),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				modifier = Modifier
					.testTag(iconTag)
					.size(TitleIconSize),
				imageVector = icon,
				tint = MaterialTheme.colorScheme.onBackground,
				contentDescription = null
			)

			Text(
				modifier = Modifier.padding(start = TuIndiceSpacing.Medium),
				text = titleText,
				style = MaterialTheme.typography.titleSmall,
				color = MaterialTheme.colorScheme.onBackground
			)
		}

		trailing()
	}
}

private val TitleIconSize = 18.dp
