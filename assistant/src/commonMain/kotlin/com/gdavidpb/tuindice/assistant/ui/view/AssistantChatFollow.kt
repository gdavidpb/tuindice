package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.presentation.model.hasIntentCard
import com.gdavidpb.tuindice.assistant.ui.style.AssistantChatMotion
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeoutOrNull

// How far past the list the end of the last item can be with the student still at the end: a progress label growing.
private val AtEndTolerance = 72.dp

// A little more than the growth of a label, so its last frames are followed too.
private const val LABEL_GROWTH_MARGIN_MILLIS = 100L

// The list goes to a new last item when the student sent it or was at the end of the conversation. Someone reading
// older messages stays where they are, and the button to the end takes them back.
@Composable
internal fun FollowConversation(listState: LazyListState, items: List<ChatItem>, progressAtEnd: Boolean) {
	val animated = LocalTuIndiceAnimationsEnabled.current
	val tolerance = with(LocalDensity.current) { AtEndTolerance.roundToPx() }
	val follow = remember(listState) { ConversationFollow() }

	LaunchedEffect(followKeyOf(items)) {
		val wasAtEnd = follow.isGoing || follow.lastIndex < 0 ||
			listState.showsEndOf(minOf(follow.lastIndex, items.lastIndex), tolerance)
		val last = items.lastOrNull()
		follow.lastIndex = items.lastIndex

		if (last != null && (last is ChatItem.User || wasAtEnd)) {
			follow.go {
				listState.followLastItem(
					lastIndex = items.lastIndex,
					showsItsEnd = last.hasIntentCard || progressAtEnd,
					animated = animated
				)
			}
		}
	}
}

// Follow a wait that starts at the end, and its label as it grows. Only a change in the size of the last item moves the
// list then, so someone who scrolls up to read older messages is left where they are.
@Composable
internal fun FollowProgressWhenAtEnd(listState: LazyListState, visible: Boolean) {
	val animated = LocalTuIndiceAnimationsEnabled.current
	val tolerance = with(LocalDensity.current) { AtEndTolerance.roundToPx() }

	LaunchedEffect(visible) {
		val lastIndex = listState.layoutInfo.totalItemsCount - 1
		if (!visible || lastIndex < 0 || !listState.showsEndOf(lastIndex, tolerance)) return@LaunchedEffect

		listState.followLastItem(lastIndex = lastIndex, showsItsEnd = true, animated = animated)
		// The label grows at the pace of the device's animation scale.
		val scale = currentCoroutineContext()[MotionDurationScale]?.scaleFactor ?: 1f
		withTimeoutOrNull(((AssistantChatMotion.SmallEnterMillis + LABEL_GROWTH_MARGIN_MILLIS) * scale).toLong()) {
			snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull { it.index == lastIndex }?.size }
				.filterNotNull()
				.distinctUntilChanged()
				.drop(1)
				.filter { !listState.isScrollInProgress }
				.collect { listState.scrollToItem(lastIndex, Int.MAX_VALUE) }
		}
	}
}

// The list follows its last item as the keyboard opens and closes, in each frame of its animation. The inset is read
// outside composition, so those frames compose nothing; a follow cancels the one before it. Other changes of the
// column's height (a suggestion, a notice) leave the list where it is.
@Composable
internal fun FollowLastItemOnKeyboard(
	listState: LazyListState,
	items: List<ChatItem>,
	progressVisible: Boolean,
	ime: WindowInsets
) {
	val latestItems by rememberUpdatedState(items)
	val latestProgressVisible by rememberUpdatedState(progressVisible)
	val animated = LocalTuIndiceAnimationsEnabled.current
	val density = LocalDensity.current

	// The first value is not followed: the conversation's effect follows it when the conversation is composed.
	LaunchedEffect(listState, ime) {
		snapshotFlow { ime.getBottom(density) }
			.distinctUntilChanged()
			.drop(1)
			.collectLatest {
				val current = latestItems

				if (current.isNotEmpty()) {
					listState.followLastItem(
						lastIndex = current.lastIndex,
						showsItsEnd = current.last().hasIntentCard || latestProgressVisible,
						animated = animated
					)
				}
			}
	}
}

// What the list follows its last item by: how many items there are, and which one is last. A change inside an item
// keeps both, so it does not move the list; a new item, or another one at the end, changes them.
internal fun followKeyOf(items: List<ChatItem>): Pair<Int, String?> = items.size to items.lastOrNull()?.id

// Show the last item from its start, or down to its end when it contains an intent card or a running label. With that
// item in view only its foot moves: going by its start first would take a tall item up and back down.
internal suspend fun LazyListState.followLastItem(lastIndex: Int, showsItsEnd: Boolean, animated: Boolean) {
	if (!animated) {
		scrollToItem(lastIndex, if (showsItsEnd) Int.MAX_VALUE else 0)
		return
	}

	if (!showsItsEnd || layoutInfo.visibleItemsInfo.none { it.index == lastIndex }) animateScrollToItem(lastIndex)
	if (showsItsEnd) {
		val layout = layoutInfo
		val lastItemInfo = layout.visibleItemsInfo.lastOrNull { it.index == lastIndex }
		val hiddenBelow = lastItemInfo?.let { it.offset + it.size - layout.viewportEndOffset } ?: 0

		if (hiddenBelow > 0) animateScrollBy(hiddenBelow.toFloat())
	}
}

private fun LazyListState.showsEndOf(index: Int, tolerance: Int): Boolean {
	val layout = layoutInfo
	val item = layout.visibleItemsInfo.lastOrNull { it.index == index } ?: return false

	return item.offset + item.size <= layout.viewportEndOffset + tolerance
}

// Where the list last went. A follow that a newer item replaces has not arrived, but the student was at the end all the
// same; a follow the student's own scroll takes over ends there.
private class ConversationFollow {
	var lastIndex = -1
	var isGoing = false
		private set

	suspend fun go(scroll: suspend () -> Unit) {
		isGoing = true
		try {
			scroll()
			isGoing = false
		} catch (exception: CancellationException) {
			if (currentCoroutineContext().isActive) isGoing = false
			throw exception
		}
	}
}
