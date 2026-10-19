package com.gdavidpb.tuindice.assistant.ui.component.sistema

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.assistant.presentation.model.UsageNoticeItem
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_usage_title

// sistema.aviso_uso/1: the requests left in each window, and when the quota resets if the service knows it.
@Composable
fun UsageNoticeComponentView(
	item: UsageNoticeItem,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier) {
		Text(
			text = stringResource(Res.string.assistant_usage_title),
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onSurface
		)
		Text(
			text = item.hourText.asString(),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface
		)
		Text(
			text = item.dayText.asString(),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface
		)
		Text(
			text = item.monthText.asString(),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface
		)
		item.resetsText?.let { resetsText ->
			Text(
				text = resetsText.asString(),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}
