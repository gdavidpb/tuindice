package com.gdavidpb.tuindice.login.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.utils.extension.stateInAction
import com.gdavidpb.tuindice.login.domain.usecase.StartUpUseCase
import kotlinx.coroutines.flow.MutableSharedFlow

class SplashViewModel(
	startUpUseCase: StartUpUseCase
) : ViewModel() {
	val fetchStartUpActionParams = MutableSharedFlow<String>()

	val fetchStartUpAction =
		stateInAction(useCase = startUpUseCase, paramsFlow = fetchStartUpActionParams)
}