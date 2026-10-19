package com.gdavidpb.tuindice.assistant.ui.component.sistema

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.assistant.presentation.model.WarningItem
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_warning_title

// sistema.advertencia/1: a warning about the reason the service gives.
@Composable
fun WarningComponentView(
	item: WarningItem,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier) {
		Text(
			text = stringResource(Res.string.assistant_warning_title),
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onSurface
		)
		Text(
			text = item.reasonText.asString(),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface
		)
	}
}
