package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextDecoration
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
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

// What the student needs to know before accepting the assistant. It says "el asistente": no name in the chat. A
// [failed] acceptance says so above the actions, and accepting again is the retry.
@Composable
fun AssistantConsentView(
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier,
	failed: Boolean = false
) {
	Column(
		modifier = modifier.fillMaxWidth().testTag(AssistantUiTags.ConsentView),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		Text(
			text = stringResource(Res.string.assistant_consent_title),
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onSurface
		)
		listOf(
			Res.string.assistant_consent_scope,
			Res.string.assistant_consent_accuracy,
			Res.string.assistant_consent_providers,
			Res.string.assistant_consent_retention,
			Res.string.assistant_consent_control
		).forEach { line ->
			Text(
				text = stringResource(line),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
		TextButton(
			onClick = { onAction(AssistantChat.Action.OpenNavTarget(NavTarget.ABOUT, emptyList())) },
			modifier = Modifier.testTag(AssistantUiTags.ConsentPrivacy)
		) {
			Text(
				text = stringResource(Res.string.assistant_consent_privacy),
				style = MaterialTheme.typography.labelLarge.copy(textDecoration = TextDecoration.Underline)
			)
		}
		if (failed) {
			Text(
				text = stringResource(Res.string.assistant_consent_failed),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.error,
				modifier = Modifier.testTag(AssistantUiTags.ConsentFailed)
			)
		}
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium, Alignment.End)
		) {
			OutlinedButton(
				onClick = { onAction(AssistantChat.Action.DeclineConsent) },
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
