package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material.icons.outlined.ToggleOff
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_consent_accept
import tuindice.assistant.generated.resources.assistant_consent_accuracy
import tuindice.assistant.generated.resources.assistant_consent_control
import tuindice.assistant.generated.resources.assistant_consent_decline
import tuindice.assistant.generated.resources.assistant_consent_failed
import tuindice.assistant.generated.resources.assistant_consent_privacy
import tuindice.assistant.generated.resources.assistant_consent_providers
import tuindice.assistant.generated.resources.assistant_consent_retention
import tuindice.assistant.generated.resources.assistant_consent_scope
import tuindice.assistant.generated.resources.assistant_consent_title

// What the assistant does, that it can be wrong, who processes the messages, how long they are kept and how to turn it
// off: one short row each, with its icon.
private val consentLines: List<Pair<ImageVector, StringResource>> = listOf(
	Icons.Outlined.QuestionAnswer to Res.string.assistant_consent_scope,
	Icons.Outlined.ErrorOutline to Res.string.assistant_consent_accuracy,
	Icons.Outlined.Cloud to Res.string.assistant_consent_providers,
	Icons.Outlined.History to Res.string.assistant_consent_retention,
	Icons.Outlined.ToggleOff to Res.string.assistant_consent_control
)

// What the student needs to know before accepting the assistant. It says "el asistente": no name in the chat. The title
// is the one of the assistant's sheets, the sparkle on its line, and «Ahora no» is the negative button of the app's
// sheets (ConfirmationDialog). The privacy policy link
// opens the app's policy, starts at the margin of the rows and keeps a touch area of 48 dp. A [failed] acceptance says
// so above the actions, and accepting again is the retry.
@Composable
fun AssistantConsentView(
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier,
	failed: Boolean = false
) {
	Column(modifier = modifier.fillMaxWidth().testTag(AssistantUiTags.ConsentView)) {
		AssistantSheetTitleView(
			text = stringResource(Res.string.assistant_consent_title),
			modifier = Modifier.padding(bottom = 16.dp).testTag(AssistantUiTags.ConsentTitle)
		)
		Column(verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XLarge)) {
			consentLines.forEach { (icon, line) ->
				AssistantConsentLineView(icon = icon, text = stringResource(line))
			}
		}
		Box(
			modifier = Modifier
				// The touch area of an icon button: 48 dp tall, with the text centred in it.
				.heightIn(min = TuIndiceComponentSizes.IconButton)
				.clickable(role = Role.Button) { onAction(AssistantChat.Action.OpenPrivacyPolicy) }
				.testTag(AssistantUiTags.ConsentPrivacy),
			contentAlignment = Alignment.CenterStart
		) {
			Text(
				text = stringResource(Res.string.assistant_consent_privacy),
				style = MaterialTheme.typography.labelLarge.copy(textDecoration = TextDecoration.Underline),
				color = MaterialTheme.colorScheme.onSurface
			)
		}
		if (failed) {
			Text(
				text = stringResource(Res.string.assistant_consent_failed),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.error,
				modifier = Modifier.padding(top = TuIndiceSpacing.Medium).testTag(AssistantUiTags.ConsentFailed)
			)
		}
		Row(
			modifier = Modifier.fillMaxWidth().padding(top = TuIndiceSpacing.Section),
			horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium, Alignment.End)
		) {
			OutlinedButton(
				onClick = { onAction(AssistantChat.Action.DeclineConsent) },
				border = null,
				modifier = Modifier.testTag(AssistantUiTags.ConsentDecline)
			) {
				Text(text = stringResource(Res.string.assistant_consent_decline))
			}
			Button(
				onClick = { onAction(AssistantChat.Action.AcceptConsent) },
				modifier = Modifier.testTag(AssistantUiTags.ConsentAccept)
			) {
				Text(text = stringResource(Res.string.assistant_consent_accept))
			}
		}
	}
}
