package com.gdavidpb.tuindice.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.presentation.action.main.CloseMainDialogActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.RequestReviewActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.RequestUpdateActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.SetLastScreenActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.StartUpActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.UpdateStateActionProcessor
import com.gdavidpb.tuindice.presentation.contract.Main
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.review.ReviewManager
import kotlinx.coroutines.flow.Flow

class MainViewModel(
	private val updateStateActionProcessor: UpdateStateActionProcessor,
	private val startUpActionProcessor: StartUpActionProcessor,
	private val requestReviewActionProcessor: RequestReviewActionProcessor,
	private val requestUpdateActionProcessor: RequestUpdateActionProcessor,
	private val setLastScreenActionProcessor: SetLastScreenActionProcessor,
	private val closeMainDialogActionProcessor: CloseMainDialogActionProcessor
) : BaseViewModel<Main.State, Main.Action, Main.Effect>(
	initialState = Main.State.Starting,
	initialAction = Main.Action.StartUp
) {

	fun updateStateAction(state: Main.State) =
		sendAction(Main.Action.UpdateState(state))

	fun requestReviewAction(reviewManager: ReviewManager) =
		sendAction(Main.Action.RequestReview(reviewManager))

	fun setLastScreenAction(route: String) =
		sendAction(Main.Action.SetLastScreen(route))

	fun checkUpdateAction(appUpdateManager: AppUpdateManager) =
		sendAction(Main.Action.RequestUpdate(appUpdateManager))

	fun closeDialogAction() =
		sendAction(Main.Action.CloseDialog)

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

			is Main.Action.RequestUpdate ->
				requestUpdateActionProcessor.process(action, sideEffect)

			is Main.Action.SetLastScreen ->
				setLastScreenActionProcessor.process(action, sideEffect)

			is Main.Action.CloseDialog ->
				closeMainDialogActionProcessor.process(action, sideEffect)
		}
	}
}
