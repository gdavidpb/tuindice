package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// One detail row of a chat card: a bold [code] (a subject code, when there is one), its [name] in the muted tone and
// the [trailing] figure or chip on the right. Rows after the first draw a hairline divider above them.
@Composable
fun CardRowView(
	name: String,
	modifier: Modifier = Modifier,
	code: String? = null,
	showsDivider: Boolean = true,
	trailing: @Composable () -> Unit = {}
) {
	Column(modifier = modifier.fillMaxWidth()) {
		if (showsDivider) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium),
			verticalAlignment = Alignment.CenterVertically
		) {
			Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Small)) {
				code?.let { text ->
					Text(text = text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
				}
				Text(
					text = name,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 2,
					overflow = TextOverflow.Ellipsis
				)
			}
			trailing()
		}
	}
}
