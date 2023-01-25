package com.gdavidpb.tuindice.login.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.utils.extension.LiveEvent
import com.gdavidpb.tuindice.base.utils.extension.execute
import com.gdavidpb.tuindice.login.domain.param.SignInParams
import com.gdavidpb.tuindice.login.domain.usecase.ReSignInUseCase
import com.gdavidpb.tuindice.login.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.login.domain.error.SignInError

class SignInViewModel(
	private val signInUseCase: SignInUseCase,
	private val reSignInUseCase: ReSignInUseCase
) : ViewModel() {
	val signIn = LiveEvent<Unit, SignInError>()

	fun signIn(usbId: String, password: String) =
		execute(useCase = signInUseCase, params = SignInParams(usbId, password), liveData = signIn)

	fun reSignIn(password: String) =
		execute(useCase = reSignInUseCase, params = password, liveData = signIn)
}