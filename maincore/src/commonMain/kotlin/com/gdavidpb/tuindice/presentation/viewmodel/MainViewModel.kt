package com.gdavidpb.tuindice.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.presentation.action.main.RequestReviewActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.RequestUpdateActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.SetLastDestinationActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.StartUpActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.UpdateStateActionProcessor
import com.gdavidpb.tuindice.presentation.contract.Main
import kotlinx.coroutines.flow.Flow

class MainViewModel(
	private val updateStateActionProcessor: UpdateStateActionProcessor,
	private val startUpActionProcessor: StartUpActionProcessor,
	private val requestReviewActionProcessor: RequestReviewActionProcessor,
	private val requestUpdateActionProcessor: RequestUpdateActionProcessor,
	private val setLastDestinationActionProcessor: SetLastDestinationActionProcessor
) : BaseViewModel<Main.State, Main.Action, Main.Effect>(
	initialState = Main.State.Starting,
	initialAction = Main.Action.StartUp
) {

	fun updateStateAction(state: Main.State) =
		sendAction(Main.Action.UpdateState(state))

	fun requestReviewAction() =
		sendAction(Main.Action.RequestReview)

	fun startUpAction() =
		sendAction(Main.Action.StartUp)

	fun setLastDestinationAction(destination: Destination) =
		sendAction(Main.Action.SetLastDestination(destination))

	fun checkUpdateAction() =
		sendAction(Main.Action.RequestUpdateCheck)

	override fun processAction(
		action: Main.Action,
		sideEffect: (Main.Effect) -> Unit
	): Flow<Mutation<Main.State>> {
		return when (action) {
			is Main.Action.UpdateState ->
				updateStateActionProcessor.process(action, sideEffect)

			is Main.Action.StartUp ->
				startUpActionProcessor.process(action, sideEffect)

			is Main.Action.RequestReview ->
				requestReviewActionProcessor.process(action, sideEffect)

			is Main.Action.RequestUpdateCheck ->
				requestUpdateActionProcessor.process(action, sideEffect)

			is Main.Action.SetLastDestination ->
				setLastDestinationActionProcessor.process(action, sideEffect)
		}
	}
}
