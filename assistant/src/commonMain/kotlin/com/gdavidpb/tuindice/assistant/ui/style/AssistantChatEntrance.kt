package com.gdavidpb.tuindice.assistant.ui.style

import androidx.compose.animation.core.Animatable
import com.gdavidpb.tuindice.base.utils.extension.DecelerateEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import kotlinx.coroutines.delay

/**
 * Fades the composable in while it rises [rise], once, [delayMillis] after it is composed. It is invisible
 * (but laid out, so nothing around it moves) until then. With [enabled] false, or with animations off, it adds nothing.
 */
@Composable
internal fun Modifier.chatEntrance(
	enabled: Boolean,
	delayMillis: Int = 0,
	durationMillis: Int = AssistantChatMotion.MessageEnterMillis,
	rise: Dp = AssistantChatMotion.MessageRise
): Modifier {
	if (!enabled || !LocalTuIndiceAnimationsEnabled.current) return this

	val progress = remember { Animatable(0f) }
	val risePx = with(LocalDensity.current) { rise.toPx() }

	LaunchedEffect(Unit) {
		delay(delayMillis.toLong())
		progress.animateTo(targetValue = 1f, animationSpec = tween(durationMillis, easing = DecelerateEasing))
	}

	return graphicsLayer {
		alpha = progress.value
		translationY = (1f - progress.value) * risePx
	}
}

/**
 * The scale of an icon that pops when [selected] changes to true (1 → 1.25 → 1 in 260 ms). It never pops on the
 * first composition, so a thumb that was already chosen when the message appeared stays still.
 */
@Composable
internal fun Modifier.selectionPop(selected: Boolean): Modifier {
	if (!LocalTuIndiceAnimationsEnabled.current) return this

	val scale = remember { Animatable(1f) }
	var wasSelected by remember { mutableStateOf(selected) }

	LaunchedEffect(selected) {
		val pops = selected && !wasSelected

		wasSelected = selected
		if (pops) {
			scale.animateTo(
				targetValue = 1f,
				animationSpec = keyframes {
					durationMillis = AssistantChatMotion.SelectionPopMillis
					AssistantChatMotion.PopScale at AssistantChatMotion.SelectionPopMillis / 2
				}
			)
		} else {
			scale.snapTo(1f)
		}
	}

	return graphicsLayer {
		scaleX = scale.value
		scaleY = scale.value
	}
}

/**
 * Which items of the conversation already were on screen. Only an item that arrives while the sheet is open plays its
 * entrance; the history present when the sheet opens appears at once, and so does an item that scrolls back into view.
 *
 * It is a plain set held by the view, not state in the machine: whether something is «new» is a matter of when this
 * sheet was opened, which the machine does not know and the conversation does not need to keep.
 */
internal class ChatEntranceLedger(initialIds: Collection<String>) {
	private val initial = initialIds.toSet()
	private val seen = initialIds.toMutableSet()

	/** True when [id] was not in the conversation when the sheet opened. */
	fun arrivedAfterOpen(id: String): Boolean = id !in initial

	/** True the first time [id] is asked about, false from then on. */
	fun isNew(id: String): Boolean = seen.add(id)
}
