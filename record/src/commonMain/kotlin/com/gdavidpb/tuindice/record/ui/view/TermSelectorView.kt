package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.presentation.model.TermItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags

@Composable
fun TermSelectorView(
	modifier: Modifier = Modifier,
	terms: List<TermItem>,
	selectedTermId: String?,
	onTermSelected: (termId: String) -> Unit
) {
	val lazyListState = rememberLazyListState()

	LaunchedEffect(
		terms.map { term -> term.termId },
		selectedTermId
	) {
		val selectedIndex = terms.indexOfFirst { term ->
			term.termId == selectedTermId
		}

		if (selectedIndex >= 0) {
			lazyListState.animateScrollToItem(selectedIndex)
		}
	}

	BoxWithConstraints(
		modifier = modifier
			.fillMaxWidth()
			.testTag(RecordUiTags.TermSelectorRow)
	) {
		val itemWidth = maxWidth * 0.5f
		val sidePeekPadding = (maxWidth - itemWidth) / 2

		LazyRow(
			modifier = Modifier.fillMaxWidth(),
			state = lazyListState,
			userScrollEnabled = false,
			contentPadding = PaddingValues(horizontal = sidePeekPadding),
			horizontalArrangement = Arrangement.spacedBy(2.dp)
		) {
			items(
				items = terms,
				key = { term -> term.termId },
				contentType = { TermSelectorItemContentType }
			) { term ->
				val isSelected = term.termId == selectedTermId
				val scale by animateFloatAsState(
					targetValue = if (isSelected) 1f else 0.95f,
					label = "term_selector_scale"
				)
				val alpha by animateFloatAsState(
					targetValue = if (isSelected) 1f else 0.76f,
					label = "term_selector_alpha"
				)

				Box(
					modifier = Modifier
						.width(itemWidth)
						.graphicsLayer {
							scaleX = scale
							scaleY = scale
							this.alpha = alpha
						}
						.semantics {
							selected = isSelected
						}
						.clickable {
							onTermSelected(term.termId)
						}
						.testTag(RecordUiTags.termChip(term.termId)),
					contentAlignment = Alignment.Center
				) {
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
		}
	}
}

private const val TermSelectorItemContentType = "term_selector_item"
