package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

private val CARD_HEIGHT = 56.dp
private val CARD_ICON_SIZE = 28.dp

// A PDF as a card: a document icon, its name on one line, and its pages under it once the service has read them. The
// card keeps the same height in the tray and in the sent message.
@Composable
fun AssistantChatPdfCardView(
	name: String,
	detail: String?,
	modifier: Modifier = Modifier
) {
	val shape = RoundedCornerShape(TuIndiceRadius.Medium)

	Row(
		modifier = modifier
			.height(CARD_HEIGHT)
			.background(MaterialTheme.colorScheme.surfaceVariant, shape)
			.border(TuIndiceSpacing.Hairline, MaterialTheme.colorScheme.outlineVariant, shape)
			.padding(horizontal = TuIndiceSpacing.Medium),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			imageVector = Icons.Outlined.Description,
			contentDescription = null,
			modifier = Modifier.size(CARD_ICON_SIZE),
			tint = MaterialTheme.colorScheme.onSurfaceVariant
		)

		Column(modifier = Modifier.weight(1f).padding(start = TuIndiceSpacing.Small)) {
			Text(
				text = name,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface
			)

			detail?.let { pages ->
				Text(
					text = pages,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
	}
}
