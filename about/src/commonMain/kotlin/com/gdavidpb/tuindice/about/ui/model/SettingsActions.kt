package com.gdavidpb.tuindice.about.ui.model

// The callbacks of the settings screen, bundled so the screen and its views take one parameter.
data class SettingsActions(
	val onXClick: () -> Unit,
	val onTermsAndConditionsClick: () -> Unit,
	val onPrivacyPolicyClick: () -> Unit,
	val onSupportClick: () -> Unit,
	val onShareAppClick: () -> Unit,
	val onRateOnPlayStoreClick: () -> Unit,
	val onReportBugClick: () -> Unit,
	val onAboutClick: () -> Unit,
	val onSignOutClick: () -> Unit,
	val onUsageDataCollectionEnabledChange: (enabled: Boolean) -> Unit,
	val onAssistantConsentChange: (granted: Boolean) -> Unit,
	val onAssistantMemoryClick: () -> Unit,
	val onAssistantConversationsClick: () -> Unit
)
