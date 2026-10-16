package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAnimation
import com.gdavidpb.tuindice.base.utils.extension.DecelerateEasing
import kotlinx.coroutines.flow.conflate

/**
 * Draws the whole card of [targetState] and, when that changes, fades the card on screen into the new one. The card
 * changes as a whole, in one fade of [TuIndiceAnimation.CrossfadeMillis] that decelerates, and nothing in it moves. A
 * card on screen that [isEmpty] gives way at once, since there is nothing on it to fade from.
 *
 * A change that comes while a fade is still running waits for that fade to end, and then fades from the card on screen
 * by then. So there are never more than two cards at once, and no fade starts from a card that is only half drawn. With
 * the animations off the new card is drawn at once, with no wrapper around it.
 */
@Composable
internal fun <T> ShareableTermCardCrossfade(
	targetState: T,
	modifier: Modifier,
	isEmpty: (T) -> Boolean,
	content: @Composable (T) -> Unit
) {
	if (!LocalTuIndiceAnimationsEnabled.current) {
		Box(modifier = modifier) {
			content(targetState)
		}

		return
	}

	// The card on screen and the card the fade is taking it to. These two are the only cards ever composed.
	val shown = remember { mutableStateOf(targetState) }
	val incoming = remember { mutableStateOf<T?>(null) }
	// How far the incoming card has faded in, from nothing to all of it.
	val fade = remember { Animatable(1f) }
	val latestTarget = rememberUpdatedState(targetState)
	val latestIsEmpty = rememberUpdatedState(isEmpty)

	LaunchedEffect(Unit) {
		snapshotFlow { latestTarget.value }
			.conflate()
			.collect { target ->
				if (target == shown.value) return@collect

				if (latestIsEmpty.value(shown.value)) {
					shown.value = target
					return@collect
				}

				// The new card starts invisible, so it is never drawn whole for a frame before the fade begins.
				fade.snapTo(0f)
				incoming.value = target
				fade.animateTo(
					targetValue = 1f,
					animationSpec = tween(
						durationMillis = TuIndiceAnimation.CrossfadeMillis,
						easing = DecelerateEasing
					)
				)

				// The card the fade has reached takes the place of the one under it before the fade is cleared, so the
				// card on screen never steps back to the old one in between.
				shown.value = target
				incoming.value = null
			}
	}

	Box(modifier = modifier) {
		val current = shown.value
		val next = incoming.value

		listOfNotNull(current, next).distinct().forEach { card ->
			key(card) {
				Box(
					modifier = Modifier
						.fillMaxSize()
						.graphicsLayer { alpha = if (card == next) fade.value else 1f }
				) {
					content(card)
				}
			}
		}
	}
}
