package com.gdavidpb.tuindice.login.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.utils.extensions.LiveResult
import com.gdavidpb.tuindice.base.domain.model.StartUpAction
import com.gdavidpb.tuindice.base.utils.extensions.execute
import com.gdavidpb.tuindice.login.domain.usecase.StartUpUseCase
import com.gdavidpb.tuindice.login.domain.usecase.errors.StartUpError

class SplashViewModel(
	private val startUpUseCase: StartUpUseCase
) : ViewModel() {
	val startUpAction = LiveResult<StartUpAction, StartUpError>()

	fun fetchStartUpAction(dataString: String) =
		execute(useCase = startUpUseCase, params = dataString, liveData = startUpAction)
}