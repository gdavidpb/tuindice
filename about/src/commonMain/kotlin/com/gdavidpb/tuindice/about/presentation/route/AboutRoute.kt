package com.gdavidpb.tuindice.about.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.about.domain.model.AboutLinks
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import com.gdavidpb.tuindice.base.domain.repository.BrowserGateway
import com.gdavidpb.tuindice.base.domain.repository.ExternalActions
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.config

private val contactEmail by config { getContactEmail() }
private val contactSubject by config { getContactSubject() }

@Composable
fun AboutRoute(
	onNavigateToBrowser: (title: String, url: String) -> Unit,
	browserGateway: BrowserGateway,
	externalActions: ExternalActions,
	viewModel: AboutViewModel,
	content: @Composable (
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
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is About.Effect.NavigateToBrowser ->
				onNavigateToBrowser(effect.title, effect.url)

			is About.Effect.StartBrowser ->
				browserGateway.open(effect.url)

			is About.Effect.ShowReportBugDialog ->
				externalActions.sendEmail(
					email = contactEmail,
					subject = contactSubject
				)

			is About.Effect.StartEmail ->
				externalActions.sendEmail(
					email = contactEmail,
					subject = contactSubject
				)

			is About.Effect.StartPlayStore ->
				externalActions.openStorePage()

			is About.Effect.StartShare ->
				externalActions.shareText(
					subject = effect.subject,
					text = effect.text
				)
		}
	}

	content(
		viewState,
		{ viewModel.openUrlAction(AboutLinks.CREATIVE_COMMONS) },
		{ viewModel.openUrlAction(AboutLinks.X) },
		{ viewModel.openUrlAction(AboutLinks.GITHUB) },
		{ viewModel.openUrlAction(AboutLinks.KOTLIN) },
		{ viewModel.openUrlAction(AboutLinks.COMPOSE) },
		{ viewModel.openUrlAction(AboutLinks.FIREBASE) },
		{ viewModel.openUrlAction(AboutLinks.KOIN) },
		{ viewModel.openUrlAction(AboutLinks.KTOR) },
		{ viewModel.openUrlAction(AboutLinks.DST) },
		viewModel::openTermsAndConditionsAction,
		viewModel::openPrivacyPolicyAction,
		viewModel::shareAppAction,
		viewModel::rateOnPlayStoreAction,
		viewModel::contactDeveloperAction,
		viewModel::reportBugAction
	)
}
