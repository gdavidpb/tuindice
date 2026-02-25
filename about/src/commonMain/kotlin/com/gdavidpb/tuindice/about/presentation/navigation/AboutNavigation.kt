package com.gdavidpb.tuindice.about.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.presentation.route.AboutRoute
import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import com.gdavidpb.tuindice.base.domain.repository.BrowserGateway
import com.gdavidpb.tuindice.base.domain.repository.ExternalActions
import org.koin.compose.koinInject

fun NavGraphBuilder.aboutNavigation(
	onNavigateToBrowser: (title: String, url: String) -> Unit,
	aboutContent: @Composable (
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
	) -> Unit
) {
	navigation<AboutDestination.NavGraph>(startDestination = AboutDestination.About) {
		composable<AboutDestination.About> {
			val browserGateway = koinInject<BrowserGateway>()
			val externalActions = koinInject<ExternalActions>()
			val viewModel = koinInject<AboutViewModel>()

			AboutRoute(
				onNavigateToBrowser = onNavigateToBrowser,
				browserGateway = browserGateway,
				externalActions = externalActions,
				viewModel = viewModel,
				content = aboutContent
			)
		}
	}
}
