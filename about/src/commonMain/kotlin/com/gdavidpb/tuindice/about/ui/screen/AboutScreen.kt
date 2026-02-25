package com.gdavidpb.tuindice.about.ui.screen

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.about.presentation.contract.About

@Composable
fun AboutScreen(
	state: About.State,
	onCreativeCommonsClick: () -> Unit,
	onXClick: () -> Unit,
	onGithubClick: () -> Unit,
	onKotlinClick: () -> Unit,
	onComposeClick: () -> Unit,
	onFirebaseClick: () -> Unit,
	onKoinClick: () -> Unit,
	onKtorClick: () -> Unit,
	onDstClick: () -> Unit,
	onTermsAndConditionsClick: () -> Unit,
	onPrivacyPolicyClick: () -> Unit,
	onShareAppClick: () -> Unit,
	onRateOnPlayStoreClick: () -> Unit,
	onContactDeveloperClick: () -> Unit,
	onReportBugClick: () -> Unit,
	contentStateContent: @Composable (
		state: About.State.Content,
		onCreativeCommonsClick: () -> Unit,
		onXClick: () -> Unit,
		onGithubClick: () -> Unit,
		onKotlinClick: () -> Unit,
		onComposeClick: () -> Unit,
		onFirebaseClick: () -> Unit,
		onKoinClick: () -> Unit,
		onKtorClick: () -> Unit,
		onDstClick: () -> Unit,
		onTermsAndConditionsClick: () -> Unit,
		onPrivacyPolicyClick: () -> Unit,
		onShareAppClick: () -> Unit,
		onRateOnPlayStoreClick: () -> Unit,
		onContactDeveloperClick: () -> Unit,
		onReportBugClick: () -> Unit
	) -> Unit
) {
	if (state !is About.State.Content) return

	contentStateContent(
		state,
		onCreativeCommonsClick,
		onXClick,
		onGithubClick,
		onKotlinClick,
		onComposeClick,
		onFirebaseClick,
		onKoinClick,
		onKtorClick,
		onDstClick,
		onTermsAndConditionsClick,
		onPrivacyPolicyClick,
		onShareAppClick,
		onRateOnPlayStoreClick,
		onContactDeveloperClick,
		onReportBugClick
	)
}
