package com.gdavidpb.tuindice.login.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.utils.extension.stateInEagerly
import com.gdavidpb.tuindice.login.domain.usecase.StartUpUseCase
import kotlinx.coroutines.flow.MutableSharedFlow

class SplashViewModel(
	startUpUseCase: StartUpUseCase
) : ViewModel() {
	val startUpAction = MutableSharedFlow<String>()

	val fetchStartUpAction =
		stateInEagerly(useCase = startUpUseCase, paramsFlow = startUpAction)
}