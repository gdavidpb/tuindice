package com.gdavidpb.tuindice.about.ui.model

// The callbacks of the about screen, bundled so the screen and its views take one parameter.
data class AboutActions(
	val onCreativeCommonsClick: () -> Unit = {},
	val onXClick: () -> Unit = {},
	val onGithubClick: () -> Unit = {},
	val onKotlinClick: () -> Unit = {},
	val onComposeClick: () -> Unit = {},
	val onFirebaseClick: () -> Unit = {},
	val onKoinClick: () -> Unit = {},
	val onKtorClick: () -> Unit = {},
	val onDstClick: () -> Unit = {},
	val onTermsAndConditionsClick: () -> Unit = {},
	val onPrivacyPolicyClick: () -> Unit = {},
	val onSupportClick: () -> Unit = {},
	val onShareAppClick: () -> Unit = {},
	val onRateOnPlayStoreClick: () -> Unit = {},
	val onContactDeveloperClick: () -> Unit = {},
	val onReportBugClick: () -> Unit = {},
	val onUsageDataCollectionEnabledChange: (enabled: Boolean) -> Unit = {},
	val onAssistantConsentChange: (granted: Boolean) -> Unit = {},
	val onAssistantMemoryClick: () -> Unit = {},
	val onAssistantConversationsClick: () -> Unit = {}
)
