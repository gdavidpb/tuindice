package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceDarkTheme
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.LottieResourceAnimationView
import org.jetbrains.compose.resources.ExperimentalResourceApi
import tuindice.assistant.generated.resources.Res

private const val ATOM_PATH = "files/an_assistant_atom.json"
private const val ATOM_LAYER_FILL = "glyph_fill"
private const val ATOM_SIZE_DP = 48
private const val ATOM_FILL_LIGHT = 0xFF785900L
private const val ATOM_FILL_DARK = 0xFFFABD00L

// The running turn: the 3D atom in the accent tones and the impersonal label that sweeps with light. Both stay still
// when animations are off.
@OptIn(ExperimentalResourceApi::class)
@Composable
fun AssistantProgressView(
	label: String,
	modifier: Modifier = Modifier
) {
	Row(
		modifier = modifier.fillMaxWidth().testTag(AssistantUiTags.ProgressContainer),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		LottieResourceAnimationView(
			readBytes = { Res.readBytes(ATOM_PATH) },
			modifier = Modifier.size(ATOM_SIZE_DP.dp),
			testTag = AssistantUiTags.ProgressAtom,
			fillOverrides = mapOf(ATOM_LAYER_FILL to atomFillColor())
		)
		AssistantShimmerLabelView(
			text = label,
			testTag = AssistantUiTags.ProgressLabel
		)
	}
}

@Composable
private fun atomFillColor(): Color =
	if (TuIndiceDarkTheme.isDark()) Color(ATOM_FILL_DARK) else Color(ATOM_FILL_LIGHT)
