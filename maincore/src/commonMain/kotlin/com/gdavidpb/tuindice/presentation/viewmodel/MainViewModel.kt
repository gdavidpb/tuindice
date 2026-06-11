package com.gdavidpb.tuindice.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.presentation.action.main.RequestReviewActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.RequestSyncActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.RequestUpdateActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.RequestWizardStartActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.SetLastMainSectionActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.StartUpActionProcessor
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.mapper.toMainSectionOrNull
import kotlinx.coroutines.flow.Flow

class MainViewModel(
	private val startUpActionProcessor: StartUpActionProcessor,
	private val requestReviewActionProcessor: RequestReviewActionProcessor,
	private val requestSyncActionProcessor: RequestSyncActionProcessor,
	private val requestUpdateActionProcessor: RequestUpdateActionProcessor,
	private val setLastMainSectionActionProcessor: SetLastMainSectionActionProcessor,
	private val requestWizardStartActionProcessor: RequestWizardStartActionProcessor,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : BaseViewModel<Main.State, Main.Action, Main.Effect>(
	name = "main",
	initialState = Main.State.Starting,
	initialAction = Main.Action.StartUp,
	dispatchers = dispatchers
) {
	fun requestReviewAction() =
		sendAction(Main.Action.RequestReview)

	fun startUpAction() =
		sendAction(Main.Action.StartUp)

	fun setLastDestinationAction(destination: Destination) {
		destination.toMainSectionOrNull()
			?.let { sendAction(Main.Action.SetLastMainSection(section = it)) }
	}

	fun checkUpdateAction() =
		sendAction(Main.Action.RequestUpdateCheck)

	fun requestSyncAction() =
		sendAction(Main.Action.RequestSync)

	fun requestWizardStartAction() =
		sendAction(Main.Action.RequestWizardStart)

	override suspend fun processAction(
		action: Main.Action,
		sideEffect: (Main.Effect) -> Unit
	): Flow<Mutation<Main.State>> {
		return when (action) {
			is Main.Action.StartUp ->
				startUpActionProcessor.process(action, sideEffect)

			is Main.Action.RequestReview ->
				requestReviewActionProcessor.process(action, sideEffect)

			is Main.Action.RequestSync ->
				requestSyncActionProcessor.process(action, sideEffect)

			is Main.Action.RequestUpdateCheck ->
				requestUpdateActionProcessor.process(action, sideEffect)

			is Main.Action.SetLastMainSection ->
				setLastMainSectionActionProcessor.process(action, sideEffect)

			is Main.Action.RequestWizardStart ->
				requestWizardStartActionProcessor.process(action, sideEffect)
		}
	}
}
