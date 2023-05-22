package com.gdavidpb.tuindice.about.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.gdavidpb.tuindice.about.BuildConfig
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import com.gdavidpb.tuindice.about.ui.screen.AboutScreen
import com.gdavidpb.tuindice.about.utils.extension.email
import com.gdavidpb.tuindice.about.utils.extension.playStore
import com.gdavidpb.tuindice.about.utils.extension.share
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.browse
import com.gdavidpb.tuindice.base.utils.extension.config
import org.koin.androidx.compose.koinViewModel

private val contactEmail by config { getContactEmail() }
private val contactSubject by config { getContactSubject() }

@Composable
fun AboutRoute(
	onNavigateToBrowser: (title: String, url: String) -> Unit,
	viewModel: AboutViewModel = koinViewModel()
) {
	val context = LocalContext.current

	CollectEffectWithLifecycle(flow = viewModel.viewEvent) { event ->
		when (event) {
			is About.Event.NavigateToPrivacyPolicy ->
				onNavigateToBrowser(event.title, event.url)

			is About.Event.NavigateToTermsAndConditions ->
				onNavigateToBrowser(event.title, event.url)

			is About.Event.StartBrowser ->
				context.browse(event.url)

			is About.Event.ShowReportBugDialog ->
				context.email(
					email = contactEmail,
					subject = contactSubject
				)

			is About.Event.StartEmail ->
				context.email(
					email = contactEmail,
					subject = contactSubject
				)

			is About.Event.StartPlayStore ->
				context.playStore()

			is About.Event.StartShare ->
				context.share(
					subject = event.subject,
					text = event.text
				)
		}
	}

	AboutScreen(
		onCreativeCommonsClick = { viewModel.openUrlAction(BuildConfig.URL_CREATIVE_COMMONS) },
		onTwitterClick = { viewModel.openUrlAction(BuildConfig.URL_TWITTER) },
		onGithubClick = { viewModel.openUrlAction(BuildConfig.URL_GITHUB) },
		onKotlinClick = { viewModel.openUrlAction(BuildConfig.URL_KOTLIN) },
		onComposeClick = { viewModel.openUrlAction(BuildConfig.URL_COMPOSE) },
		onFirebaseClick = { viewModel.openUrlAction(BuildConfig.URL_FIREBASE) },
		onKoinClick = { viewModel.openUrlAction(BuildConfig.URL_KOIN) },
		onRetrofitClick = { viewModel.openUrlAction(BuildConfig.URL_RETROFIT) },
		onDstClick = { viewModel.openUrlAction(BuildConfig.URL_DST) },
		onTermsAndConditionsClick = viewModel::openTermsAndConditionsAction,
		onPrivacyPolicyClick = viewModel::openPrivacyPolicyAction,
		onShareAppClick = viewModel::shareAppAction,
		onRateOnPlayStoreClick = viewModel::rateOnPlayStoreAction,
		onContactDeveloperClick = viewModel::contactDeveloperAction,
		onReportBugClick = viewModel::reportBugAction
	)
}