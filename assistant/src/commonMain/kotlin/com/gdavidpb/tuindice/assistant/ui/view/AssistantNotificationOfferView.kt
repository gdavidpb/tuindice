package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.icon.assistantSparkleTint
import com.gdavidpb.tuindice.base.ui.icon.rememberAssistantSparkleIcon
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_notification_offer_activate
import tuindice.assistant.generated.resources.assistant_notification_offer_later
import tuindice.assistant.generated.resources.assistant_notification_offer_text
import tuindice.assistant.generated.resources.assistant_notification_offer_title

// The card that offers the notification permission in the empty chat, above the conversation: a sparkle with the
// title, the reason in one line, and the two answers at the end. «Activar avisos» is the filled one, the primary
// tone; «Ahora no» is the text button. Both hide the card; the first one also asks the system for the permission.
@Composable
fun AssistantNotificationOfferView(
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.NotificationOffer)) {
		Row(
			horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Large),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				imageVector = rememberAssistantSparkleIcon(),
				contentDescription = null,
				modifier = Modifier.size(TuIndiceComponentSizes.Icon),
				tint = assistantSparkleTint()
			)
			Text(
				text = stringResource(Res.string.assistant_notification_offer_title),
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onSurface
			)
		}
		Text(
			text = stringResource(Res.string.assistant_notification_offer_text),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium, Alignment.End)
		) {
			TextButton(
				onClick = { onAction(AssistantChat.Action.DismissNotificationOffer) },
				modifier = Modifier.testTag(AssistantUiTags.NotificationOfferLater)
			) {
				Text(text = stringResource(Res.string.assistant_notification_offer_later))
			}
			Button(
				onClick = { onAction(AssistantChat.Action.ActivateNotifications) },
				modifier = Modifier.testTag(AssistantUiTags.NotificationOfferActivate)
			) {
				Text(text = stringResource(Res.string.assistant_notification_offer_activate))
			}
		}
	}
}
