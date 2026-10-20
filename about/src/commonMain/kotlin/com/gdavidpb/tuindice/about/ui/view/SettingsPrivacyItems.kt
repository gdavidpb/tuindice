package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Subject
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.gdavidpb.tuindice.about.presentation.contract.Settings
import com.gdavidpb.tuindice.about.ui.SettingsUiTags
import com.gdavidpb.tuindice.about.ui.model.SettingsActions
import com.gdavidpb.tuindice.base.ui.view.SettingsLinkRow
import org.jetbrains.compose.resources.stringResource
import tuindice.about.generated.resources.Res
import tuindice.about.generated.resources.settings_privacy_policy
import tuindice.about.generated.resources.settings_terms_and_conditions
import tuindice.about.generated.resources.settings_usage_data_consent

@Composable
fun SettingsPrivacyItems(
	state: Settings.State.Content,
	actions: SettingsActions
) {
	// Usage data reads as a chart, so the sparkle stays the assistant's alone.
	AboutSwitchItem(
		icon = rememberVectorPainter(Icons.Outlined.Insights),
		text = stringResource(Res.string.settings_usage_data_consent),
		checked = state.usageDataCollectionEnabled,
		onCheckedChange = actions.onUsageDataCollectionEnabledChange,
		testTag = SettingsUiTags.UsageDataConsentToggle,
		tint = MaterialTheme.colorScheme.onSurfaceVariant
	)

	SettingsLinkRow(
		icon = rememberVectorPainter(Icons.Outlined.Lock),
		text = stringResource(Res.string.settings_privacy_policy),
		testTag = SettingsUiTags.OpenPrivacy,
		onClick = actions.onPrivacyPolicyClick
	)

	SettingsLinkRow(
		icon = rememberVectorPainter(Icons.AutoMirrored.Outlined.Subject),
		text = stringResource(Res.string.settings_terms_and_conditions),
		testTag = SettingsUiTags.OpenTerms,
		onClick = actions.onTermsAndConditionsClick
	)
}
