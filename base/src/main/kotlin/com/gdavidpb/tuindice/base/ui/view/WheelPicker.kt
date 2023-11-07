package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

object WheelPickerDefaults {
	const val AdditionalItemCount = 3
}

@Composable
fun WheelPicker(
	modifier: Modifier = Modifier,
	count: Int,
	enabled: Boolean = true,
	state: LazyListState = rememberLazyListState(),
	itemHeight: Dp,
	additionalItemCount: Int = WheelPickerDefaults.AdditionalItemCount,
	itemValidator: (index: Int) -> Boolean = { true },
	onItemPicked: (index: Int) -> Unit,
	content: @Composable BoxScope.(index: Int) -> Unit
) {
	val coroutineScope = rememberCoroutineScope()
	val currentOnItemPicked by rememberUpdatedState(onItemPicked)
	val currentIndex = remember { mutableIntStateOf(state.firstVisibleItemIndex) }

	val itemHalfHeight = with(LocalDensity.current) { itemHeight.toPx() / 2 }

	LaunchedEffect(state.isScrollInProgress) {
		if (!state.isScrollInProgress && state.firstVisibleItemScrollOffset != 0) {
			if (state.firstVisibleItemScrollOffset < itemHalfHeight)
				state.animateScrollToItem(state.firstVisibleItemIndex)
			else
				state.animateScrollToItem(state.firstVisibleItemIndex + 1)
		}
	}

	LaunchedEffect(state) {
		snapshotFlow { state.isScrollInProgress }
			.filter { isScrollInProgress -> !isScrollInProgress && state.firstVisibleItemScrollOffset == 0 }
			.drop(1)
			.collect {
				val index = state.firstVisibleItemIndex + additionalItemCount

				if (itemValidator(index)) {
					currentIndex.intValue = state.firstVisibleItemIndex

					currentOnItemPicked(index)
				} else {
					state.animateScrollToItem(currentIndex.intValue)
				}
			}
	}

	LazyColumn(
		modifier = modifier
			.height(
				height = (itemHeight * (additionalItemCount * 2 + 1))
			),
		state = state,
		userScrollEnabled = enabled
	) {
		items(
			count = count,
			key = { item -> item }
		) { counter ->
			Box(
				modifier = Modifier
					.height(height = itemHeight)
					.clickable {
						if (enabled)
							if (counter - additionalItemCount >= 0)
								coroutineScope.launch {
									state.animateScrollToItem(counter - additionalItemCount)
								}
					}
			) {
				content(counter)
			}
		}
	}
}