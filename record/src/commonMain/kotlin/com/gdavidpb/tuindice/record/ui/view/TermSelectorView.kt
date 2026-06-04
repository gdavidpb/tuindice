package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.view.PeekingSelectorView
import com.gdavidpb.tuindice.record.presentation.model.TermItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags

@Composable
fun TermSelectorView(
	modifier: Modifier = Modifier,
	terms: List<TermItem>,
	selectedTermId: String?,
	onTermSelected: (termId: String) -> Unit
) {
	PeekingSelectorView(
		modifier = modifier
			.fillMaxWidth()
			.testTag(RecordUiTags.TermSelectorRow),
		items = terms,
		selectedItemKey = selectedTermId,
		itemKey = { term -> term.termId },
		itemTestTag = { term -> RecordUiTags.termChip(term.termId) },
		onItemSelected = { term -> onTermSelected(term.termId) },
		contentType = TermSelectorItemContentType
	) { term, isSelected ->
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.Center,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				modifier = Modifier.weight(1f, fill = false),
				text = term.shortNameText,
				style = MaterialTheme.typography.titleLarge,
				fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
				color = if (isSelected) {
					MaterialTheme.colorScheme.onSurface
				} else {
					MaterialTheme.colorScheme.onSurfaceVariant
				},
				textAlign = TextAlign.Center,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)

			if (term.isCurrent) {
				Box(
					modifier = Modifier
						.padding(start = 8.dp)
						.size(8.dp)
						.clip(CircleShape)
						.background(MaterialTheme.colorScheme.primary)
						.testTag(RecordUiTags.termCurrentChip(term.termId))
				)
			}
		}
	}
}

private const val TermSelectorItemContentType = "term_selector_item"
