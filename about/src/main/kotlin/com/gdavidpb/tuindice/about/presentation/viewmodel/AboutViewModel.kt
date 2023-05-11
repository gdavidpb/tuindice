package com.gdavidpb.tuindice.about.presentation.viewmodel

import com.gdavidpb.tuindice.about.BuildConfig
import com.gdavidpb.tuindice.about.R
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.base.utils.ResourceResolver

class AboutViewModel(
	private val resourceResolver: ResourceResolver
) :
	BaseViewModel<About.State, About.Action, About.Event>(initialViewState = About.State.Idle) {

	fun openTermsAndConditionsAction() =
		emitAction(About.Action.OpenTermsAndConditions)

	fun openPrivacyPolicyAction() =
		emitAction(About.Action.OpenPrivacyPolicy)

	fun shareAppAction() =
		emitAction(About.Action.ShareApp)

	fun rateOnPlayStoreAction() =
		emitAction(About.Action.RateOnPlayStore)

	fun reportBugAction() =
		emitAction(About.Action.ReportBug)

	fun contactDeveloperAction() =
		emitAction(About.Action.ContactDeveloper)

	fun openUrlAction(url: String) =
		emitAction(About.Action.OpenUrl(url))

	override suspend fun reducer(action: About.Action) {
		when (action) {
			is About.Action.OpenTermsAndConditions ->
				sendEvent(About.Event.NavigateToTermsAndConditions)

			is About.Action.OpenPrivacyPolicy ->
				sendEvent(About.Event.NavigateToPrivacyPolicy)

			is About.Action.ShareApp ->
				sendEvent(
					About.Event.StartShare(
						text = resourceResolver.getString(
							R.string.about_share_message,
							BuildConfig.APPLICATION_ID
						),
						subject = resourceResolver.getString(R.string.app_name)
					)
				)

			is About.Action.RateOnPlayStore ->
				sendEvent(About.Event.StartPlayStore)

			is About.Action.ReportBug ->
				sendEvent(About.Event.ShowReportBugDialog)

			is About.Action.ContactDeveloper ->
				sendEvent(About.Event.StartEmail)

			is About.Action.OpenUrl ->
				sendEvent(About.Event.Browse(url = action.url))
		}
	}
}