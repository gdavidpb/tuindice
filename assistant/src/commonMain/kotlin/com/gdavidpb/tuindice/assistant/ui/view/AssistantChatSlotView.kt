package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.assistant.ui.style.AssistantChatMotion
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// A place of the conversation's column that comes and goes (the context chip, the offer, the progress label, the
// notice, the suggestions). It carries the 16 dp that separates it from what is above it, so that, while it is gone,
// it leaves no gap behind: a column that spaced its children would keep the gap of an empty slot.
//
// [visible] true enters with a fade and a growth in height (200 ms) and false leaves with a fade and a shrink (150 ms);
// a slot that is visible when it is first composed is simply there. With [fadeOnly] it does not change height when it
// leaves, only fades out in 150 ms, and it enters with no transition at all (the suggestions do their own, chip by
// chip). With animations off it is composed or not, at once, with no transition wrapper.
//
// Hold the content that a leaving slot shows with rememberLastNonNull, so it does not go blank while it leaves.
@Composable
internal fun AssistantChatSlotView(
	visible: Boolean,
	modifier: Modifier = Modifier,
	fadeOnly: Boolean = false,
	content: @Composable () -> Unit
) {
	if (!LocalTuIndiceAnimationsEnabled.current) {
		if (visible) Column(modifier = modifier.padding(top = TuIndiceSpacing.Medium)) { content() }
		return
	}

	val enter = if (fadeOnly) {
		EnterTransition.None
	} else {
		fadeIn(tween(AssistantChatMotion.SmallEnterMillis)) +
			expandVertically(tween(AssistantChatMotion.SmallEnterMillis), expandFrom = Alignment.Top)
	}
	val exit = if (fadeOnly) {
		fadeOut(tween(AssistantChatMotion.ExitMillis))
	} else {
		fadeOut(tween(AssistantChatMotion.ExitMillis)) +
			shrinkVertically(tween(AssistantChatMotion.ExitMillis), shrinkTowards = Alignment.Top)
	}

	AnimatedVisibility(visible = visible, modifier = modifier, enter = enter, exit = exit) {
		Column(modifier = Modifier.padding(top = TuIndiceSpacing.Medium)) { content() }
	}
}
