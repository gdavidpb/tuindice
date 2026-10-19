package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.about.ui.model.AboutActions
import org.jetbrains.compose.resources.stringResource
import tuindice.about.generated.resources.Res
import tuindice.about.generated.resources.about_usage_data_consent

@Composable
fun AboutPreferenceItems(
	state: About.State.Content,
	actions: AboutActions
) {
	// Usage data reads as a chart, so the sparkle stays the assistant's alone.
	AboutSwitchItem(
		icon = rememberVectorPainter(Icons.Outlined.Insights),
		text = stringResource(Res.string.about_usage_data_consent),
		checked = state.usageDataCollectionEnabled,
		onCheckedChange = actions.onUsageDataCollectionEnabledChange,
		testTag = AboutUiTags.UsageDataConsentToggle,
		tint = MaterialTheme.colorScheme.onSurfaceVariant
	)

	if (state.assistantAvailable) {
		AboutAssistantItems(
			consentGranted = state.assistantConsentGranted,
			onConsentChange = actions.onAssistantConsentChange,
			onMemoryClick = actions.onAssistantMemoryClick,
			onConversationsClick = actions.onAssistantConversationsClick
		)
	}
}
