package com.gdavidpb.tuindice.base.presentation.viewmodel

import androidx.annotation.IdRes
import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.domain.usecase.GetUpdateInfoUseCase
import com.gdavidpb.tuindice.base.domain.usecase.RequestReviewUseCase
import com.gdavidpb.tuindice.base.domain.usecase.SetLastScreenUseCase
import com.gdavidpb.tuindice.base.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.base.utils.extensions.LiveCompletable
import com.gdavidpb.tuindice.base.utils.extensions.LiveEvent
import com.gdavidpb.tuindice.base.utils.extensions.execute
import com.gdavidpb.tuindice.base.utils.extensions.postSuccess
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager

class MainViewModel(
	private val signOutUseCase: SignOutUseCase,
	private val setLastScreenUseCase: SetLastScreenUseCase,
	private val requestReviewUseCase: RequestReviewUseCase,
	private val getUpdateInfoUseCase: GetUpdateInfoUseCase
) : ViewModel() {
	val signOut = LiveCompletable<Nothing>()
	val lastScreen = LiveCompletable<Nothing>()
	val requestReview = LiveEvent<ReviewInfo, Nothing>()
	val updateInfo = LiveEvent<AppUpdateInfo, Nothing>()
	val outdatedPassword = LiveEvent<Unit, Nothing>()

	fun outdatedPassword() =
		outdatedPassword.postSuccess(Unit)

	fun signOut() =
		execute(useCase = signOutUseCase, params = Unit, liveData = signOut)

	fun setLastScreen(@IdRes navId: Int) =
		execute(useCase = setLastScreenUseCase, params = navId, liveData = lastScreen)

	fun checkReview(reviewManager: ReviewManager) =
		execute(useCase = requestReviewUseCase, params = reviewManager, liveData = requestReview)

	fun checkUpdate(updateManager: AppUpdateManager) =
		execute(useCase = getUpdateInfoUseCase, params = updateManager, liveData = updateInfo)
}
