package com.gdavidpb.tuindice.about.ui.screen

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.ui.view.AboutContentView
import com.gdavidpb.tuindice.about.ui.view.AboutIdleView

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
	onReportBugClick: () -> Unit
) {
	when (state) {
		is About.State.Idle ->
			AboutIdleView()

		is About.State.Content ->
			AboutContentView(
				state = state,
				onCreativeCommonsClick = onCreativeCommonsClick,
				onXClick = onXClick,
				onGithubClick = onGithubClick,
				onKotlinClick = onKotlinClick,
				onComposeClick = onComposeClick,
				onFirebaseClick = onFirebaseClick,
				onKoinClick = onKoinClick,
				onKtorClick = onKtorClick,
				onDstClick = onDstClick,
				onTermsAndConditionsClick = onTermsAndConditionsClick,
				onPrivacyPolicyClick = onPrivacyPolicyClick,
				onShareAppClick = onShareAppClick,
				onRateOnPlayStoreClick = onRateOnPlayStoreClick,
				onContactDeveloperClick = onContactDeveloperClick,
				onReportBugClick = onReportBugClick
			)
	}
}
