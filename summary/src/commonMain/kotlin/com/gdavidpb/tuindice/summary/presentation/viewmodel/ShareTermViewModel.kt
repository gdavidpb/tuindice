package com.gdavidpb.tuindice.summary.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel
import com.gdavidpb.tuindice.summary.presentation.contract.ShareTerm
import com.gdavidpb.tuindice.summary.presentation.machine.ShareTermMachine
import com.gdavidpb.tuindice.summary.presentation.model.ShareCardPicture

class ShareTermViewModel(
	override val screenMachine: ShareTermMachine,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<ShareTerm.State, ShareTerm.Action, ShareTerm.Effect>(
	name = "summary_share_term",
	initialState = screenMachine.initialState(),
	dispatchers = dispatchers
) {
	fun observeShareTermAction(termKey: String?) =
		sendAction(ShareTerm.Action.ObserveShareTerm(termKey = termKey))

	fun selectTermAction(termKey: String) =
		sendAction(ShareTerm.Action.SelectTerm(termKey = termKey))

	fun selectBackgroundAction(backgroundId: String) =
		sendAction(ShareTerm.Action.SelectBackground(backgroundId = backgroundId))

	// The captured card goes to the machine, which encodes it as a PNG on the background dispatcher before sharing it.
	fun shareImageAction(picture: ShareCardPicture) =
		sendAction(ShareTerm.Action.ShareImage(picture = picture))
}
