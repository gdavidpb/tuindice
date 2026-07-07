package com.gdavidpb.tuindice.about.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.about.domain.model.AboutLinks
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.presentation.utils.LocalShareTextHandler
import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import com.gdavidpb.tuindice.about.ui.screen.AboutScreen
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import org.jetbrains.compose.resources.stringResource
import tuindice.about.generated.resources.Res
import tuindice.about.generated.resources.snack_mail_app_missing
import tuindice.about.generated.resources.snack_open_uri_failed

@Composable
fun AboutRoute(
	onNavigateToBrowser: (title: String, url: String) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: AboutViewModel
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()
	val uriHandler = LocalUriHandler.current
	val shareTextHandler = LocalShareTextHandler.current
	val mailAppMissingMessage = stringResource(Res.string.snack_mail_app_missing)
	val openUriFailedMessage = stringResource(Res.string.snack_open_uri_failed)

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is About.Effect.NavigateToBrowser ->
				onNavigateToBrowser(effect.title, effect.url)

			is About.Effect.OpenUri ->
				runCatching { uriHandler.openUri(effect.uri) }
					.recoverCatching { throwable ->
						val fallbackUri = effect.uri.toStoreWebUri() ?: throw throwable

						uriHandler.openUri(fallbackUri)
					}
					.onFailure {
						showSnackBar(
							SnackBarMessage(
								message = if (effect.uri.startsWith(MAILTO_SCHEME)) {
									mailAppMissingMessage
								} else {
									openUriFailedMessage
								}
							)
						)
					}

			is About.Effect.ShareText ->
				shareTextHandler(
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
		onSupportClick = viewModel::openSupportAction,
		onShareAppClick = viewModel::shareAppAction,
		onRateOnPlayStoreClick = viewModel::rateOnPlayStoreAction,
		onContactDeveloperClick = viewModel::contactDeveloperAction,
		onReportBugClick = viewModel::reportBugAction,
		onUsageDataCollectionEnabledChange = viewModel::setUsageDataCollectionEnabledAction
	)
}

private const val MAILTO_SCHEME = "mailto:"
private const val MARKET_SCHEME = "market://"
private const val STORE_WEB_BASE = "https://play.google.com/store/apps/"

private fun String.toStoreWebUri(): String? =
	takeIf { startsWith(MARKET_SCHEME) }?.replace(MARKET_SCHEME, STORE_WEB_BASE)
