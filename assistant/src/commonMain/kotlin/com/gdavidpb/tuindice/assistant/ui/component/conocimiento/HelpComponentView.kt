package com.gdavidpb.tuindice.assistant.ui.component.conocimiento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.HelpItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardBodyTextView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkItemView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardTitleView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_help_label

// conocimiento.ayuda/1: the title of the help, its steps numbered in order, and a link to the screen it names, when it
// names one.
@Composable
fun HelpComponentView(
	item: HelpItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Knowledge.help(item.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_b_help_label))
		CardTitleView(text = item.title)
		item.steps.forEach { step ->
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Small),
				verticalAlignment = Alignment.Top
			) {
				Text(
					text = step.numberText.asString(),
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
				CardBodyTextView(
					text = step.text,
					modifier = Modifier.weight(1f)
				)
			}
		}
		item.openLink?.let { link ->
			CardLinkItemView(
				link = link,
				onAction = onAction,
				modifier = Modifier.testTag(AssistantUiTags.Knowledge.helpOpen(item.id))
			)
		}
	}
}
