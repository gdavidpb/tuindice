package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.SubjectCodeChip

// One detail row of a chat card: its [code] or its [codes], its [title] and its [name], with the [trailing] figures and
// chips on the right. The trailing side takes at most half the row, so it wraps onto a second line before it takes the
// room of the name. A row after the first has a hairline divider above it, with a little air under it.
@Composable
fun CardRowView(
	name: String,
	code: String? = null,
	title: String? = null,
	showsDivider: Boolean = false,
	trailing: (@Composable () -> Unit)? = null
) = CardRowLayout(
	text = CardRowText(name = name, codes = listOfNotNull(code), title = title),
	showsDivider = showsDivider,
	trailing = trailing,
	onClick = null,
	onClickLabel = null
)

// The row of several subject codes, as the subjects of a clash: one code chip each, in order. The chips wrap onto a
// second line before they take the room of the name.
@Composable
fun CardRowView(
	name: String,
	codes: List<String>,
	title: String? = null,
	showsDivider: Boolean = false,
	trailing: (@Composable () -> Unit)? = null
) = CardRowLayout(
	text = CardRowText(name = name, codes = codes, title = title),
	showsDivider = showsDivider,
	trailing = trailing,
	onClick = null,
	onClickLabel = null
)

// The row of a chat card with an optional tap. A row with an [onClick] is a link to a screen: the whole row is the tap,
// [onClickLabel] is what a screen reader says it opens, and an arrow after the trailing side says so.
@Composable
internal fun CardRowLayout(
	text: CardRowText,
	showsDivider: Boolean,
	trailing: (@Composable () -> Unit)?,
	onClick: (() -> Unit)?,
	onClickLabel: String?
) {
	Column(modifier = Modifier.fillMaxWidth()) {
		if (showsDivider) {
			HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
			Spacer(modifier = Modifier.height(TuIndiceSpacing.Small))
		}

		BoxWithConstraints(modifier = rowModifier(onClick = onClick, onClickLabel = onClickLabel)) {
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
					if (text.codes.isNotEmpty()) {
						FlowRow(
							horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Small),
							verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)
						) {
							text.codes.forEach { subjectCode -> SubjectCodeChip(subjectCode = subjectCode) }
						}
					}

					Column(modifier = Modifier.weight(1f)) {
						text.title?.let { title ->
							Text(
								text = title,
								style = MaterialTheme.typography.bodyMedium,
								fontWeight = FontWeight.Bold,
								color = MaterialTheme.colorScheme.onSurface
							)
						}
						if (text.name.isNotEmpty()) {
							Text(
								text = text.name,
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					}
				}

				trailing?.let { content ->
					Column(modifier = Modifier.widthIn(max = trailingMaxWidth)) { content() }
				}

				if (onClick != null) {
					Icon(
						modifier = Modifier.size(TuIndiceComponentSizes.IconMedium),
						imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onSurface.copy(alpha = TuIndiceAlpha.Deemphasis)
					)
				}
			}
		}
	}
}

// The modifier of a row. A link takes the tap over the whole row, with its ripple clipped to the rounded shape of the
// link, and it is at least as tall as a touch target, as the shared chevron link is.
private fun rowModifier(onClick: (() -> Unit)?, onClickLabel: String?): Modifier {
	val row = Modifier.fillMaxWidth()

	return if (onClick == null) {
		row
	} else {
		row
			.clip(RoundedCornerShape(TuIndiceRadius.Small))
			.clickable(role = Role.Button, onClickLabel = onClickLabel, onClick = onClick)
			.minimumInteractiveComponentSize()
	}
}

// The share of the row the trailing side may take. The name keeps the rest, so a long figure never takes its room.
private const val TRAILING_MAX_SHARE = 0.5f
