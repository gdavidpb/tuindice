package com.gdavidpb.tuindice.assistant.ui.component.sistema

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.assistant.presentation.model.WarningItem
import com.gdavidpb.tuindice.assistant.ui.component.common.CardBodyTextView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardTitleView
import com.gdavidpb.tuindice.assistant.ui.model.CardTitleTone
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_warning_title

// sistema.advertencia/1: a warning about the reason the service gives. Its title takes the warning tone, with a warning
// mark in front of it; the card stays neutral.
@Composable
fun WarningComponentView(
	item: WarningItem,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier) {
		CardTitleView(
			text = stringResource(Res.string.assistant_warning_title),
			tone = CardTitleTone.Warning,
			icon = Icons.Outlined.WarningAmber
		)
		CardBodyTextView(text = item.reasonText.asString())
	}
}
