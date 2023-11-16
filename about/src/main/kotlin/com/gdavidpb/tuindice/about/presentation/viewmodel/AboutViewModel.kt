package com.gdavidpb.tuindice.about.presentation.viewmodel

import com.gdavidpb.tuindice.about.presentation.action.ContactDeveloperActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.OpenPrivacyPolicyActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.OpenTermsAndConditionsActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.OpenUrlActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.RateOnPlayStoreActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.ReportBugActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.ShareAppActionProcessor
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.Flow

class AboutViewModel(
	private val contactDeveloperActionProcessor: ContactDeveloperActionProcessor,
	private val openTermsAndConditionsActionProcessor: OpenTermsAndConditionsActionProcessor,
	private val openPrivacyPolicyActionProcessor: OpenPrivacyPolicyActionProcessor,
	private val shareAppActionProcessor: ShareAppActionProcessor,
	private val rateOnPlayStoreActionProcessor: RateOnPlayStoreActionProcessor,
	private val reportBugActionProcessor: ReportBugActionProcessor,
	private val openUrlActionProcessor: OpenUrlActionProcessor
) : BaseViewModel<About.State, About.Action, About.Effect>(initialState = About.State.Idle) {

	fun openTermsAndConditionsAction() =
		sendAction(About.Action.OpenTermsAndConditions)

	fun openPrivacyPolicyAction() =
		sendAction(About.Action.OpenPrivacyPolicy)

	fun shareAppAction() =
		sendAction(About.Action.ShareApp)

	fun rateOnPlayStoreAction() =
		sendAction(About.Action.RateOnPlayStore)

	fun reportBugAction() =
		sendAction(About.Action.ReportBug)

	fun contactDeveloperAction() =
		sendAction(About.Action.ContactDeveloper)

	fun openUrlAction(url: String) =
		sendAction(About.Action.OpenUrl(url))

	override fun processAction(
		action: About.Action,
		sideEffect: (About.Effect) -> Unit
	): Flow<Mutation<About.State>> {
		return when (action) {
			is About.Action.ContactDeveloper ->
				contactDeveloperActionProcessor.process(action, sideEffect)

			is About.Action.OpenPrivacyPolicy ->
				openPrivacyPolicyActionProcessor.process(action, sideEffect)

			is About.Action.OpenTermsAndConditions ->
				openTermsAndConditionsActionProcessor.process(action, sideEffect)

			is About.Action.RateOnPlayStore ->
				rateOnPlayStoreActionProcessor.process(action, sideEffect)

			is About.Action.ReportBug ->
				reportBugActionProcessor.process(action, sideEffect)

			is About.Action.ShareApp ->
				shareAppActionProcessor.process(action, sideEffect)

			is About.Action.OpenUrl ->
				openUrlActionProcessor.process(action, sideEffect)
		}
	}
}