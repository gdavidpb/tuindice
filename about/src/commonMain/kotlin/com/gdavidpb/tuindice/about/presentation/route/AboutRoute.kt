package com.gdavidpb.tuindice.about.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.about.domain.repository.ExternalActionsRepository
import com.gdavidpb.tuindice.about.domain.model.AboutLinks
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.about.ui.screen.AboutScreen
import org.koin.compose.koinInject

@Composable
fun AboutRoute(
	onNavigateToBrowser: (title: String, url: String) -> Unit,
	viewModel: AboutViewModel,
	externalActionsRepository: ExternalActionsRepository = koinInject()
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()
	val uriHandler = LocalUriHandler.current

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is About.Effect.NavigateToBrowser ->
				onNavigateToBrowser(effect.title, effect.url)

			is About.Effect.OpenUri ->
				uriHandler.openUri(effect.uri)

			is About.Effect.ShareText ->
				externalActionsRepository.shareText(
					subject = effect.subject,
					text = effect.text
				)
		}
	}

	AboutScreen(
		state = viewState,
		onCreativeCommonsClick = { viewModel.openUrlAction(AboutLinks.CREATIVE_COMMONS) },
		onXClick = { viewModel.openUrlAction(AboutLinks.X) },
		onGithubClick = { viewModel.openUrlAction(AboutLinks.GITHUB) },
		onKotlinClick = { viewModel.openUrlAction(AboutLinks.KOTLIN) },
		onComposeClick = { viewModel.openUrlAction(AboutLinks.COMPOSE) },
		onFirebaseClick = { viewModel.openUrlAction(AboutLinks.FIREBASE) },
		onKoinClick = { viewModel.openUrlAction(AboutLinks.KOIN) },
		onKtorClick = { viewModel.openUrlAction(AboutLinks.KTOR) },
		onDstClick = { viewModel.openUrlAction(AboutLinks.DST) },
		onTermsAndConditionsClick = viewModel::openTermsAndConditionsAction,
		onPrivacyPolicyClick = viewModel::openPrivacyPolicyAction,
		onShareAppClick = viewModel::shareAppAction,
		onRateOnPlayStoreClick = viewModel::rateOnPlayStoreAction,
		onContactDeveloperClick = viewModel::contactDeveloperAction,
		onReportBugClick = viewModel::reportBugAction
	)
}
