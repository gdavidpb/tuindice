package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.gdavidpb.tuindice.about.ui.SettingsUiTags
import com.gdavidpb.tuindice.base.ui.icon.assistantSparkleTint
import com.gdavidpb.tuindice.base.ui.icon.rememberAssistantSparkleIcon
import com.gdavidpb.tuindice.base.ui.view.SettingsLinkRow
import org.jetbrains.compose.resources.stringResource
import tuindice.about.generated.resources.Res
import tuindice.about.generated.resources.settings_assistant_consent
import tuindice.about.generated.resources.settings_assistant_conversations
import tuindice.about.generated.resources.settings_assistant_memory

@Composable
fun SettingsAssistantItems(
	consentGranted: Boolean,
	onConsentChange: (granted: Boolean) -> Unit,
	onMemoryClick: () -> Unit,
	onConversationsClick: () -> Unit
) {
	AboutSwitchItem(
		icon = rememberVectorPainter(rememberAssistantSparkleIcon()),
		text = stringResource(Res.string.settings_assistant_consent),
		checked = consentGranted,
		onCheckedChange = onConsentChange,
		testTag = SettingsUiTags.AssistantConsentSwitch,
		// The sparkle is in the accent of the app, as in the top bar and the swipe actions.
		tint = assistantSparkleTint()
	)

	SettingsLinkRow(
		icon = rememberVectorPainter(Icons.Outlined.Psychology),
		text = stringResource(Res.string.settings_assistant_memory),
		testTag = SettingsUiTags.AssistantMemoryItem,
		onClick = onMemoryClick
	)

	SettingsLinkRow(
		icon = rememberVectorPainter(Icons.Outlined.Forum),
		text = stringResource(Res.string.settings_assistant_conversations),
		testTag = SettingsUiTags.AssistantConversationsItem,
		onClick = onConversationsClick
	)
}
