package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.gdavidpb.tuindice.assistant.ui.model.AssistantMessageActionSizes
import com.gdavidpb.tuindice.assistant.ui.style.AssistantChatMotion
import com.gdavidpb.tuindice.assistant.ui.style.selectionPop
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled

// The icon of an action that has a chosen state (a thumb, the flag): outlined once not chosen, filled
// once chosen, in the [tint] the caller picks for each state. The tint eases and the glyph crossfades
// in 150 ms, and the icon pops once when it becomes chosen (never on first composition, so a thumb
// already chosen when the message appears stays still). With animations off it simply swaps.
@Composable
internal fun AssistantStateIconView(
	isSelected: Boolean,
	selectedIcon: ImageVector,
	unselectedIcon: ImageVector,
	tint: Color,
	contentDescription: String
) {
	if (!LocalTuIndiceAnimationsEnabled.current) {
		Icon(
			imageVector = if (isSelected) selectedIcon else unselectedIcon,
			contentDescription = contentDescription,
			modifier = Modifier.size(AssistantMessageActionSizes.Icon),
			tint = tint
		)
		return
	}

	val animatedTint by animateColorAsState(
		targetValue = tint,
		animationSpec = tween(AssistantChatMotion.ExitMillis),
		label = "AssistantStateIconTint"
	)

	Box(modifier = Modifier.selectionPop(isSelected)) {
		Crossfade(
			targetState = isSelected,
			animationSpec = tween(AssistantChatMotion.ExitMillis),
			label = "AssistantStateIconGlyph"
		) { selected ->
			Icon(
				imageVector = if (selected) selectedIcon else unselectedIcon,
				contentDescription = contentDescription,
				modifier = Modifier.size(AssistantMessageActionSizes.Icon),
				tint = animatedTint
			)
		}
	}
}
