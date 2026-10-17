package com.gdavidpb.tuindice.assistant.ui.component.conocimiento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.chatcontract.component.HelpComponent
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_help_label
import tuindice.assistant.generated.resources.assistant_card_b_help_open
import tuindice.assistant.generated.resources.assistant_card_b_help_step

// conocimiento.ayuda/1: the title of the help, its steps numbered in order, and a button to the screen it names, when
// it names one.
@Composable
fun HelpComponentView(
	component: HelpComponent,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Knowledge.help(component.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_b_help_label))
		Text(
			text = component.title,
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onSurface
		)
		component.steps.forEachIndexed { index, step ->
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Small),
				verticalAlignment = Alignment.Top
			) {
				Text(
					text = stringResource(Res.string.assistant_card_b_help_step, index + 1),
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
				Text(
					text = step,
					modifier = Modifier.weight(1f),
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurface
				)
			}
		}
		component.target?.let { target ->
			OutlinedButton(
				onClick = { onAction(AssistantChat.Action.OpenNavTarget(target, component.entityRefs)) },
				modifier = Modifier.testTag(AssistantUiTags.Knowledge.helpOpen(component.id))
			) {
				Text(text = stringResource(Res.string.assistant_card_b_help_open))
			}
		}
	}
}
