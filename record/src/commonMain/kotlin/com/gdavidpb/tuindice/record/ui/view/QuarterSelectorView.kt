package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.presentation.model.QuarterItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlin.math.abs

@Composable
fun QuarterSelectorView(
	modifier: Modifier = Modifier,
	quarters: List<QuarterItem>,
	selectedQuarterId: String?,
	onQuarterSelected: (quarterId: String) -> Unit
) {
	val lazyListState = rememberLazyListState()

	LaunchedEffect(
		quarters.map { quarter -> quarter.quarterId },
		selectedQuarterId
	) {
		val selectedIndex = quarters.indexOfFirst { quarter ->
			quarter.quarterId == selectedQuarterId
		}

		if (selectedIndex >= 0) {
			lazyListState.scrollToItem(selectedIndex)
		}
	}

	LaunchedEffect(
		lazyListState,
		quarters.map { quarter -> quarter.quarterId },
		selectedQuarterId
	) {
		snapshotFlow { lazyListState.isScrollInProgress }
			.filter { isScrollInProgress -> !isScrollInProgress }
			.mapNotNull {
				centeredQuarterId(
					lazyListState = lazyListState,
					quarters = quarters
				)
			}
			.distinctUntilChanged()
			.collect { centeredQuarterId ->
				if (centeredQuarterId != selectedQuarterId) {
					onQuarterSelected(centeredQuarterId)
				}
			}
	}

	BoxWithConstraints(
		modifier = modifier
			.fillMaxWidth()
			.padding(vertical = 8.dp)
			.testTag(RecordUiTags.QuarterSelectorRow)
	) {
		val itemWidth = maxWidth * 0.56f
		val sidePeekPadding = (maxWidth - itemWidth) / 2

		LazyRow(
			modifier = Modifier.fillMaxWidth(),
			state = lazyListState,
			contentPadding = PaddingValues(horizontal = sidePeekPadding),
			horizontalArrangement = Arrangement.spacedBy(2.dp),
			flingBehavior = rememberSnapFlingBehavior(lazyListState)
		) {
			items(
				items = quarters,
				key = { quarter -> quarter.quarterId }
			) { quarter ->
				val isSelected = quarter.quarterId == selectedQuarterId
				val scale by animateFloatAsState(
					targetValue = if (isSelected) 1f else 0.95f,
					label = "quarter_selector_scale"
				)
				val alpha by animateFloatAsState(
					targetValue = if (isSelected) 1f else 0.76f,
					label = "quarter_selector_alpha"
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
							onQuarterSelected(quarter.quarterId)
						}
						.testTag(RecordUiTags.quarterChip(quarter.quarterId))
						.padding(vertical = 10.dp),
					contentAlignment = Alignment.Center
				) {
					Column(
						horizontalAlignment = Alignment.CenterHorizontally,
						verticalArrangement = Arrangement.spacedBy(6.dp)
					) {
						Text(
							text = quarter.shortNameText,
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
						CurrentQuarterChipSlot(
							quarter = quarter,
							label = currentQuarterChipLabel,
							height = 28.dp
						)
					}
				}
			}
		}
	}
}

private fun centeredQuarterId(
	lazyListState: LazyListState,
	quarters: List<QuarterItem>
): String? {
	val layoutInfo = lazyListState.layoutInfo
	val visibleItems = layoutInfo.visibleItemsInfo

	if (visibleItems.isEmpty()) return null

	val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
	val centeredItem = visibleItems.minByOrNull { item ->
		abs((item.offset + item.size / 2) - viewportCenter)
	} ?: return null

	return quarters.getOrNull(centeredItem.index)?.quarterId
}
