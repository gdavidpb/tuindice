package com.gdavidpb.tuindice.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.base.utils.extension.collect
import com.gdavidpb.tuindice.domain.usecase.GetUpdateInfoUseCase
import com.gdavidpb.tuindice.domain.usecase.RequestReviewUseCase
import com.gdavidpb.tuindice.domain.usecase.SetLastScreenUseCase
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.reducer.GetUpdateInfoReducer
import com.gdavidpb.tuindice.presentation.reducer.RequestReviewReducer
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.review.ReviewManager
import kotlinx.coroutines.flow.collect

class MainViewModel(
	private val requestReviewUseCase: RequestReviewUseCase,
	private val setLastScreenUseCase: SetLastScreenUseCase,
	private val getUpdateInfoUseCase: GetUpdateInfoUseCase,
	private val requestReviewReducer: RequestReviewReducer,
	private val getUpdateInfoReducer: GetUpdateInfoReducer
) : BaseViewModel<Main.State, Main.Action, Main.Event>(initialViewState = Main.State.Idle) {

	fun requestReviewAction(reviewManager: ReviewManager) =
		emitAction(Main.Action.RequestReview(reviewManager))

	fun setLastScreenAction(screen: Int) =
		emitAction(Main.Action.SetLastScreen(screen))

	fun checkUpdateAction(appUpdateManager: AppUpdateManager) =
		emitAction(Main.Action.RequestUpdate(appUpdateManager))

	override suspend fun reducer(action: Main.Action) {
		when (action) {
			is Main.Action.RequestReview ->
				requestReviewUseCase
					.execute(params = action.reviewManager)
					.collect(viewModel = this, reducer = requestReviewReducer)

			is Main.Action.SetLastScreen ->
				setLastScreenUseCase
					.execute(params = action.screen)
					.collect()

			is Main.Action.RequestUpdate ->
				getUpdateInfoUseCase
					.execute(params = action.appUpdateManager)
					.collect(viewModel = this, reducer = getUpdateInfoReducer)
		}
	}
}
