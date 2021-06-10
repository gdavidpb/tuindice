package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.annotation.IdRes
import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.usecase.*
import com.gdavidpb.tuindice.domain.usecase.errors.SyncError
import com.gdavidpb.tuindice.utils.extensions.LiveCompletable
import com.gdavidpb.tuindice.utils.extensions.LiveEvent
import com.gdavidpb.tuindice.utils.extensions.LiveResult
import com.gdavidpb.tuindice.utils.extensions.execute
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager

class MainViewModel(
    private val signOutUseCase: SignOutUseCase,
    private val syncUseCase: SyncUseCase,
    private val setLastScreenUseCase: SetLastScreenUseCase,
    private val requestReviewUseCase: RequestReviewUseCase,
    private val getUpdateInfoUseCase: GetUpdateInfoUseCase
) : ViewModel() {
    val signOut = LiveCompletable<Nothing>()
    val sync = LiveResult<Boolean, SyncError>()
    val lastScreen = LiveCompletable<Nothing>()
    val requestReview = LiveEvent<ReviewInfo, Nothing>()
    val updateInfo = LiveEvent<AppUpdateInfo, Nothing>()

    fun signOut() =
            execute(useCase = signOutUseCase, params = Unit, liveData = signOut)

    fun sync() =
            execute(useCase = syncUseCase, params = Unit, liveData = sync)

    fun setLastScreen(@IdRes navId: Int) =
            execute(useCase = setLastScreenUseCase, params = navId, liveData = lastScreen)

    fun checkReview(reviewManager: ReviewManager) =
            execute(useCase = requestReviewUseCase, params = reviewManager, liveData = requestReview)

    fun checkUpdate(updateManager: AppUpdateManager) =
            execute(useCase = getUpdateInfoUseCase, params = updateManager, liveData = updateInfo)
}
