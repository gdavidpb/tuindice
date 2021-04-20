package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.annotation.IdRes
import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.usecase.RequestReviewUseCase
import com.gdavidpb.tuindice.domain.usecase.SetLastScreenUseCase
import com.gdavidpb.tuindice.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.domain.usecase.SyncAccountUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.SyncError
import com.gdavidpb.tuindice.utils.extensions.LiveCompletable
import com.gdavidpb.tuindice.utils.extensions.LiveResult
import com.gdavidpb.tuindice.utils.extensions.execute
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager

class MainViewModel(
        private val signOutUseCase: SignOutUseCase,
        private val syncAccountUseCase: SyncAccountUseCase,
        private val setLastScreenUseCase: SetLastScreenUseCase,
        private val requestReviewUseCase: RequestReviewUseCase
) : ViewModel() {
    val signOut = LiveCompletable<Nothing>()
    val sync = LiveResult<Boolean, SyncError>()
    val lastScreen = LiveCompletable<Nothing>()
    val requestReview = LiveResult<ReviewInfo, Nothing>()

    fun signOut() =
            execute(useCase = signOutUseCase, params = Unit, liveData = signOut)

    fun trySyncAccount() =
            execute(useCase = syncAccountUseCase, params = Unit, liveData = sync)

    fun setLastScreen(@IdRes navId: Int) =
            execute(useCase = setLastScreenUseCase, params = navId, liveData = lastScreen)

    fun checkReview(reviewManager: ReviewManager) =
            execute(useCase = requestReviewUseCase, params = reviewManager, liveData = requestReview)
}
