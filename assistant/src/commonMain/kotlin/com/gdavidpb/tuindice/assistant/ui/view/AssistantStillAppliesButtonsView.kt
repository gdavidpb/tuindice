package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_still_applies_no
import tuindice.assistant.generated.resources.assistant_still_applies_yes

// The two answers to "¿Sigue aplicando?": «Ya no aplica» (secondary) and «Sí, sigue» (primary, on the right).
@Composable
fun AssistantStillAppliesButtonsView(
	memoryId: String,
	onAnswer: (applies: Boolean) -> Unit,
	enabled: Boolean = true,
	modifier: Modifier = Modifier
) {
	Row(
		modifier = modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium, Alignment.End)
	) {
		OutlinedButton(
			onClick = { onAnswer(false) },
			enabled = enabled,
			modifier = Modifier.testTag(AssistantUiTags.Memory.stillAppliesNo(memoryId))
		) {
			Text(text = stringResource(Res.string.assistant_still_applies_no))
		}
		Button(
			onClick = { onAnswer(true) },
			enabled = enabled,
			modifier = Modifier.testTag(AssistantUiTags.Memory.stillAppliesYes(memoryId))
		) {
			Text(text = stringResource(Res.string.assistant_still_applies_yes))
		}
	}
}
