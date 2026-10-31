package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem

// Follow a wait that starts at the end, without pulling someone who has scrolled up to read older messages.
@Composable
internal fun FollowProgressWhenAtEnd(listState: LazyListState, items: List<ChatItem>, visible: Boolean) {
	LaunchedEffect(visible) {
		if (visible && items.isNotEmpty() && !listState.canScrollForward) {
			listState.followLastItem(lastIndex = items.lastIndex, showsItsEnd = true)
		}
	}
}

// Show the last item from its start, and its end too when it contains an intent card or a running label.
internal suspend fun LazyListState.followLastItem(lastIndex: Int, showsItsEnd: Boolean) {
	animateScrollToItem(lastIndex)
	if (!showsItsEnd) return

	val layout = layoutInfo
	val lastItemInfo = layout.visibleItemsInfo.lastOrNull { it.index == lastIndex }
	val hiddenBelow = lastItemInfo?.let { it.offset + it.size - layout.viewportEndOffset } ?: 0

	if (hiddenBelow > 0) animateScrollBy(hiddenBelow.toFloat())
}
