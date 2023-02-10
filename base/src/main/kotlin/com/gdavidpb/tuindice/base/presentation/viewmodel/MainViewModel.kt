package com.gdavidpb.tuindice.base.presentation.viewmodel

import androidx.annotation.IdRes
import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.domain.usecase.GetUpdateInfoUseCase
import com.gdavidpb.tuindice.base.domain.usecase.RequestReviewUseCase
import com.gdavidpb.tuindice.base.domain.usecase.SetLastScreenUseCase
import com.gdavidpb.tuindice.base.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.base.utils.extension.*
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import kotlinx.coroutines.flow.MutableSharedFlow

class MainViewModel(
	signOutUseCase: SignOutUseCase,
	private val setLastScreenUseCase: SetLastScreenUseCase,
	private val requestReviewUseCase: RequestReviewUseCase,
	private val getUpdateInfoUseCase: GetUpdateInfoUseCase
) : ViewModel() {
	val lastScreen = LiveCompletable<Nothing>()
	val requestReview = LiveEvent<ReviewInfo, Nothing>()
	val updateInfo = LiveEvent<AppUpdateInfo, Nothing>()
	val outdatedPassword = LiveEvent<Unit, Nothing>()
	val signOutParams = MutableSharedFlow<Unit>()

	fun outdatedPassword() =
		outdatedPassword.postSuccess(Unit)

	val signOut =
		stateInEagerly(useCase = signOutUseCase, paramsFlow = signOutParams)

	fun setLastScreen(@IdRes navId: Int) =
		execute(useCase = setLastScreenUseCase, params = navId, liveData = lastScreen)

	fun checkReview(reviewManager: ReviewManager) =
		execute(useCase = requestReviewUseCase, params = reviewManager, liveData = requestReview)

	fun checkUpdate(updateManager: AppUpdateManager) =
		execute(useCase = getUpdateInfoUseCase, params = updateManager, liveData = updateInfo)
}
