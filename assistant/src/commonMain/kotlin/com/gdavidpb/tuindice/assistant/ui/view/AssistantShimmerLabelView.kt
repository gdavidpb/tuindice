package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled

internal const val SWEEP_MILLIS = 1600
internal const val SWEEP_TRAVEL = 2f
internal const val SWEEP_HIGHLIGHT_STOP = 0.5f

// The label of a running turn with a light that sweeps across it. With animations off it is plain text.
@Composable
fun AssistantShimmerLabelView(
	text: String,
	modifier: Modifier = Modifier,
	testTag: String? = null
) {
	val taggedModifier = if (testTag != null) modifier.testTag(testTag) else modifier

	if (LocalTuIndiceAnimationsEnabled.current) {
		SweepingLabel(text = text, modifier = taggedModifier)
	} else {
		Text(
			text = text,
			modifier = taggedModifier,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}

