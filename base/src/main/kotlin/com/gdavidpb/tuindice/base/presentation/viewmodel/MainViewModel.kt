package com.gdavidpb.tuindice.base.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.domain.usecase.GetUpdateInfoUseCase
import com.gdavidpb.tuindice.base.domain.usecase.RequestReviewUseCase
import com.gdavidpb.tuindice.base.domain.usecase.SetLastScreenUseCase
import com.gdavidpb.tuindice.base.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.base.utils.extension.emit
import com.gdavidpb.tuindice.base.utils.extension.emptyStateFlow
import com.gdavidpb.tuindice.base.utils.extension.stateInAction
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.review.ReviewManager

class MainViewModel(
	signOutUseCase: SignOutUseCase,
	setLastScreenUseCase: SetLastScreenUseCase,
	requestReviewUseCase: RequestReviewUseCase,
	getUpdateInfoUseCase: GetUpdateInfoUseCase
) : ViewModel() {
	private val checkUpdateParams = emptyStateFlow<AppUpdateManager>()
	private val checkReviewParams = emptyStateFlow<ReviewManager>()
	private val setLastScreenParams = emptyStateFlow<Int>()
	private val signOutParams = emptyStateFlow<Unit>()

	fun checkUpdate(appUpdateManager: AppUpdateManager) =
		emit(checkUpdateParams, appUpdateManager)

	fun checkReview(reviewManager: ReviewManager) =
		emit(checkReviewParams, reviewManager)

	fun setLastScreen(screen: Int) =
		emit(setLastScreenParams, screen)

	fun signOut() =
		emit(signOutParams, Unit)

	fun outdatedPassword() =
		emit(outdatedPassword, Unit)

	val outdatedPassword =
		emptyStateFlow<Unit>()

	val checkUpdate =
		stateInAction(useCase = getUpdateInfoUseCase, paramsFlow = checkUpdateParams)

	val checkReview =
		stateInAction(useCase = requestReviewUseCase, paramsFlow = checkReviewParams)

	val setLastScreen =
		stateInAction(useCase = setLastScreenUseCase, paramsFlow = setLastScreenParams)

	val signOut =
		stateInAction(useCase = signOutUseCase, paramsFlow = signOutParams)
}
