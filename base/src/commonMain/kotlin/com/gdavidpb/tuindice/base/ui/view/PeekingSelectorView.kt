package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun <T> PeekingSelectorView(
	items: List<T>,
	selectedItemKey: Any?,
	itemKey: (T) -> Any,
	onItemSelected: (T) -> Unit,
	modifier: Modifier = Modifier,
	itemTestTag: ((T) -> String)? = null,
	itemWidthFraction: Float = DefaultItemWidthFraction,
	horizontalItemSpacing: Dp = DefaultHorizontalItemSpacing,
	userScrollEnabled: Boolean = false,
	contentType: Any? = null,
	itemContent: @Composable BoxScope.(item: T, isSelected: Boolean) -> Unit
) {
	val lazyListState = rememberLazyListState()
	val itemKeys = items.map(itemKey)
	val hasPositionedSelectedItem = remember { mutableStateOf(false) }

	LaunchedEffect(itemKeys, selectedItemKey) {
		val selectedIndex = itemKeys.indexOf(selectedItemKey)

		if (selectedIndex >= 0) {
			if (hasPositionedSelectedItem.value) {
				lazyListState.animateScrollToItem(selectedIndex)
			} else {
				lazyListState.scrollToItem(selectedIndex)
				hasPositionedSelectedItem.value = true
			}
		}
	}

	BoxWithConstraints(
		modifier = modifier.fillMaxWidth()
	) {
		val itemWidth = maxWidth * itemWidthFraction
		val sidePeekPadding = (maxWidth - itemWidth) / 2

		LazyRow(
			modifier = Modifier.fillMaxWidth(),
			state = lazyListState,
			userScrollEnabled = userScrollEnabled,
			contentPadding = PaddingValues(horizontal = sidePeekPadding),
			horizontalArrangement = Arrangement.spacedBy(horizontalItemSpacing)
		) {
			items(
				items = items,
				key = itemKey,
				contentType = { contentType }
			) { item ->
				val itemKeyValue = itemKey(item)
				val isSelected = itemKeyValue == selectedItemKey
				val scale by animateFloatAsState(
					targetValue = if (isSelected) 1f else UnselectedScale,
					label = "peeking_selector_scale"
				)
				val alpha by animateFloatAsState(
					targetValue = if (isSelected) 1f else UnselectedAlpha,
					label = "peeking_selector_alpha"
				)
				val testTagModifier = itemTestTag
					?.let { tagProvider -> Modifier.testTag(tagProvider(item)) }
					?: Modifier

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
							onItemSelected(item)
						}
						.then(testTagModifier),
					contentAlignment = Alignment.Center
				) {
					itemContent(item, isSelected)
				}
			}
		}
	}
}

private const val DefaultItemWidthFraction = 0.5f
private val DefaultHorizontalItemSpacing = 2.dp
private const val UnselectedScale = 0.95f
private const val UnselectedAlpha = 0.76f
