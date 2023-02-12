package com.gdavidpb.tuindice.base.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.domain.usecase.GetUpdateInfoUseCase
import com.gdavidpb.tuindice.base.domain.usecase.RequestReviewUseCase
import com.gdavidpb.tuindice.base.domain.usecase.SetLastScreenUseCase
import com.gdavidpb.tuindice.base.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.base.utils.extension.stateInEagerly
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.review.ReviewManager
import kotlinx.coroutines.flow.MutableSharedFlow

class MainViewModel(
	signOutUseCase: SignOutUseCase,
	setLastScreenUseCase: SetLastScreenUseCase,
	requestReviewUseCase: RequestReviewUseCase,
	getUpdateInfoUseCase: GetUpdateInfoUseCase
) : ViewModel() {
	val signOutParams = MutableSharedFlow<Unit>()
	val checkUpdateParams = MutableSharedFlow<AppUpdateManager>()
	val checkReviewParams = MutableSharedFlow<ReviewManager>()
	val setLastScreenParams = MutableSharedFlow<Int>()
	val outdatedPassword = MutableSharedFlow<Unit>()

	val signOut =
		stateInEagerly(useCase = signOutUseCase, paramsFlow = signOutParams)

	val setLastScreen =
		stateInEagerly(useCase = setLastScreenUseCase, paramsFlow = setLastScreenParams)

	val checkReview =
		stateInEagerly(useCase = requestReviewUseCase, paramsFlow = checkReviewParams)

	val checkUpdate =
		stateInEagerly(useCase = getUpdateInfoUseCase, paramsFlow = checkUpdateParams)
}
