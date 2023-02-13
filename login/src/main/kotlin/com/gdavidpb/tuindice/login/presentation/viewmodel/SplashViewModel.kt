package com.gdavidpb.tuindice.login.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.utils.extension.emit
import com.gdavidpb.tuindice.base.utils.extension.emptyStateFlow
import com.gdavidpb.tuindice.base.utils.extension.stateInAction
import com.gdavidpb.tuindice.login.domain.usecase.StartUpUseCase

class SplashViewModel(
	startUpUseCase: StartUpUseCase
) : ViewModel() {
	private val fetchStartUpActionParams = emptyStateFlow<String>()

	fun fetchStartUpAction(dataString: String) =
		emit(fetchStartUpActionParams, dataString)

	val fetchStartUpAction =
		stateInAction(useCase = startUpUseCase, paramsFlow = fetchStartUpActionParams)
}