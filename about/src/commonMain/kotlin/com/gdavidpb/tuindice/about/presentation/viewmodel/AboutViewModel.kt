package com.gdavidpb.tuindice.about.presentation.viewmodel

import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.presentation.machine.AboutMachine
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel

class AboutViewModel(
	override val screenMachine: AboutMachine,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<About.State, About.Action, About.Effect>(
	name = "about",
	initialState = screenMachine.initialState(),
	initialAction = About.Action.LoadVersion,
	dispatchers = dispatchers
) {
	fun openTermsAndConditionsAction() =
		sendAction(About.Action.OpenTermsAndConditions)

	fun openPrivacyPolicyAction() =
		sendAction(About.Action.OpenPrivacyPolicy)

	fun openSupportAction() =
		sendAction(About.Action.OpenSupport)

	fun shareAppAction() =
		sendAction(About.Action.ShareApp)

	fun rateOnPlayStoreAction() =
		sendAction(About.Action.RateOnStore)

	fun reportBugAction() =
		sendAction(About.Action.ReportBug)

	fun contactDeveloperAction() =
		sendAction(About.Action.ContactDeveloper)

	fun openUrlAction(url: String) =
		sendAction(About.Action.OpenUrl(url))

	fun setUsageDataCollectionEnabledAction(enabled: Boolean) =
		sendAction(About.Action.SetUsageDataCollectionEnabled(enabled))
}
