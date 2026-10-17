package com.gdavidpb.tuindice.assistant.ui.component.sistema

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.assistant.ui.model.chatDateText
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.chatcontract.component.UsageNoticeComponent
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_usage_day
import tuindice.assistant.generated.resources.assistant_usage_hour
import tuindice.assistant.generated.resources.assistant_usage_month
import tuindice.assistant.generated.resources.assistant_usage_resets
import tuindice.assistant.generated.resources.assistant_usage_title

// sistema.aviso_uso/1: the requests left in each window, and when the quota resets if the service knows it.
@Composable
fun UsageNoticeComponentView(
	component: UsageNoticeComponent,
	modifier: Modifier = Modifier
) {
	val usage = component.usage
	val resetsAtMillis = component.resetsAtMillis

	AssistantEmbeddedCardView(modifier = modifier) {
		Text(
			text = stringResource(Res.string.assistant_usage_title),
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onSurface
		)
		Text(
			text = stringResource(Res.string.assistant_usage_hour, usage.remainingHour),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface
		)
		Text(
			text = stringResource(Res.string.assistant_usage_day, usage.remainingDay),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface
		)
		Text(
			text = stringResource(Res.string.assistant_usage_month, usage.remainingMonth),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface
		)
		if (resetsAtMillis != null) {
			Text(
				text = stringResource(Res.string.assistant_usage_resets, resetsAtMillis.chatDateText()),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}
