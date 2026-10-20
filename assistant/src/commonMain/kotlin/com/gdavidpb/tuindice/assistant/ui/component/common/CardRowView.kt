package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.SubjectCodeChip

// One detail row of a chat card: its [code] (a subject code, drawn as the app draws one) or its [title] (a bold text
// that is not a code, such as the title of a warning or a plan), its [name] in the muted tone, and the [trailing]
// figures and chips on the right. A text takes up to two lines and never ends in an ellipsis. The trailing side takes
// at most half the row, so it wraps onto a second line before it takes the room of the name. A row after the first has
// a hairline divider above it, with a little air under it.
@Composable
fun CardRowView(
	name: String,
	code: String? = null,
	title: String? = null,
	showsDivider: Boolean = false,
	trailing: (@Composable () -> Unit)? = null
) {
	Column(modifier = Modifier.fillMaxWidth()) {
		if (showsDivider) {
			HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
			Spacer(modifier = Modifier.height(TuIndiceSpacing.Small))
		}

		BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
			val trailingMaxWidth = maxWidth * TRAILING_MAX_SHARE

			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium),
				verticalAlignment = Alignment.CenterVertically
			) {
				Row(
					modifier = Modifier.weight(1f),
					horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Small),
					verticalAlignment = Alignment.CenterVertically
				) {
					code?.let { subjectCode -> SubjectCodeChip(subjectCode = subjectCode) }

					Column(modifier = Modifier.weight(1f)) {
						title?.let { text ->
							Text(
								text = text,
								style = MaterialTheme.typography.bodyMedium,
								fontWeight = FontWeight.Bold,
								color = MaterialTheme.colorScheme.onSurface,
								maxLines = 2
							)
						}
						if (name.isNotEmpty()) {
							Text(
								text = name,
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurfaceVariant,
								maxLines = 2
							)
						}
					}
				}

				trailing?.let { content ->
					Column(modifier = Modifier.widthIn(max = trailingMaxWidth)) { content() }
				}
			}
		}
	}
}

// The share of the row the trailing side may take. The name keeps the rest, so a long figure never takes its room.
private const val TRAILING_MAX_SHARE = 0.5f
