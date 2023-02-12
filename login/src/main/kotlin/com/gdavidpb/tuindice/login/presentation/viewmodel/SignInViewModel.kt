package com.gdavidpb.tuindice.login.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.utils.extension.stateInAction
import com.gdavidpb.tuindice.login.domain.param.SignInParams
import com.gdavidpb.tuindice.login.domain.usecase.ReSignInUseCase
import com.gdavidpb.tuindice.login.domain.usecase.SignInUseCase
import kotlinx.coroutines.flow.MutableSharedFlow

class SignInViewModel(
	signInUseCase: SignInUseCase,
	reSignInUseCase: ReSignInUseCase
) : ViewModel() {
	val signInParams = MutableSharedFlow<SignInParams>()
	val reSignInParams = MutableSharedFlow<String>()

	val signIn =
		stateInAction(useCase = signInUseCase, paramsFlow = signInParams)

	val reSignIn =
		stateInAction(useCase = reSignInUseCase, paramsFlow = reSignInParams)
}