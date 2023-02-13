package com.gdavidpb.tuindice.login.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.utils.extension.emit
import com.gdavidpb.tuindice.base.utils.extension.emptyStateFlow
import com.gdavidpb.tuindice.base.utils.extension.stateInAction
import com.gdavidpb.tuindice.login.domain.param.SignInParams
import com.gdavidpb.tuindice.login.domain.usecase.ReSignInUseCase
import com.gdavidpb.tuindice.login.domain.usecase.SignInUseCase

class SignInViewModel(
	signInUseCase: SignInUseCase,
	reSignInUseCase: ReSignInUseCase
) : ViewModel() {
	private val signInParams = emptyStateFlow<SignInParams>()
	private val reSignInParams = emptyStateFlow<String>()

	fun signIn(params: SignInParams) =
		emit(signInParams, params)

	fun reSignIn(password: String) =
		emit(reSignInParams, password)

	val signIn =
		stateInAction(useCase = signInUseCase, paramsFlow = signInParams)

	val reSignIn =
		stateInAction(useCase = reSignInUseCase, paramsFlow = reSignInParams)
}