package com.gdavidpb.tuindice.about.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.about.domain.model.AboutLinks
import com.gdavidpb.tuindice.about.presentation.contract.Settings
import com.gdavidpb.tuindice.about.presentation.navigation.SettingsNavDependencies
import com.gdavidpb.tuindice.about.presentation.utils.LocalShareTextHandler
import com.gdavidpb.tuindice.about.presentation.viewmodel.SettingsViewModel
import com.gdavidpb.tuindice.about.ui.model.SettingsActions
import com.gdavidpb.tuindice.about.ui.screen.SettingsScreen
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle

@Composable
fun SettingsRoute(
	dependencies: SettingsNavDependencies,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: SettingsViewModel
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()
	val openUri = rememberExternalUriOpener(showSnackBar = showSnackBar)
	val shareTextHandler = LocalShareTextHandler.current

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is Settings.Effect.NavigateToBrowser -> dependencies.onNavigateToBrowser(effect.title, effect.url)
			is Settings.Effect.OpenUri -> openUri(effect.uri)
			is Settings.Effect.NavigateToAbout -> dependencies.onNavigateToAbout()
			is Settings.Effect.RequestSignOut -> dependencies.onSignOutRequest()
			is Settings.Effect.NavigateToAssistantMemory -> dependencies.onNavigateToAssistantMemory()
			is Settings.Effect.NavigateToDeleteAssistantConversationsConfirmation ->
				dependencies.onNavigateToDeleteConfirmation()
			is Settings.Effect.ShowSnackBar -> showSnackBar(SnackBarMessage(message = effect.message))
			is Settings.Effect.ShareText -> shareTextHandler(subject = effect.subject, text = effect.text)
		}
	}

	SettingsScreen(
		state = viewState,
		actions = viewModel.toSettingsActions(),
		profilePhoto = dependencies.profilePhoto
	)
}

private fun SettingsViewModel.toSettingsActions() = SettingsActions(
	onXClick = { dispatchAction(Settings.Action.OpenUrl(AboutLinks.X)) },
	onTermsAndConditionsClick = { dispatchAction(Settings.Action.OpenTermsAndConditions) },
	onPrivacyPolicyClick = { dispatchAction(Settings.Action.OpenPrivacyPolicy) },
	onSupportClick = { dispatchAction(Settings.Action.OpenSupport) },
	onShareAppClick = { dispatchAction(Settings.Action.ShareApp) },
	onRateOnPlayStoreClick = { dispatchAction(Settings.Action.RateOnStore) },
	onReportBugClick = { dispatchAction(Settings.Action.ReportBug) },
	onAboutClick = { dispatchAction(Settings.Action.OpenAbout) },
	onSignOutClick = { dispatchAction(Settings.Action.RequestSignOut) },
	onUsageDataCollectionEnabledChange = { enabled ->
		dispatchAction(Settings.Action.SetUsageDataCollectionEnabled(enabled))
	},
	onAssistantConsentChange = { granted -> dispatchAction(Settings.Action.SetAssistantConsent(granted)) },
	onAssistantMemoryClick = { dispatchAction(Settings.Action.OpenAssistantMemory) },
	onAssistantConversationsClick = { dispatchAction(Settings.Action.RequestDeleteAssistantConversations) }
)
