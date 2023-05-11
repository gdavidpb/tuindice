package com.gdavidpb.tuindice.about.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import com.gdavidpb.tuindice.about.ui.screen.AboutScreen
import com.gdavidpb.tuindice.about.utils.extension.browse
import com.gdavidpb.tuindice.about.utils.extension.email
import com.gdavidpb.tuindice.about.utils.extension.playStore
import com.gdavidpb.tuindice.about.utils.extension.share
import com.gdavidpb.tuindice.about.utils.extension.showReportSelector
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.config
import org.koin.androidx.compose.koinViewModel

private val contactEmail by config { getContactEmail() }
private val contactSubject by config { getContactSubject() }
private val issuesList by config { getIssuesList() }

@Composable
fun AboutRoute(viewModel: AboutViewModel = koinViewModel()) {
	val context = LocalContext.current

	CollectEffectWithLifecycle(flow = viewModel.viewEvent) { event ->
		when (event) {
			is About.Event.Browse ->
				context.browse(
					url = event.url
				)

			is About.Event.NavigateToPrivacyPolicy ->
				TODO()

			is About.Event.NavigateToTermsAndConditions ->
				TODO()

			is About.Event.ShowReportBugDialog ->
				context.showReportSelector(
					contactEmail = contactEmail,
					issuesList = issuesList
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
		onUrlClick = viewModel::openUrlAction,
		onTermsAndConditionsClick = viewModel::openTermsAndConditionsAction,
		onPrivacyPolicyClick = viewModel::openPrivacyPolicyAction,
		onShareAppClick = viewModel::shareAppAction,
		onRateOnPlayStoreClick = viewModel::rateOnPlayStoreAction,
		onContactDeveloperClick = viewModel::contactDeveloperAction,
		onReportBugClick = viewModel::reportBugAction
	)
}