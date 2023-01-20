package com.gdavidpb.tuindice.login.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.utils.extensions.LiveEvent
import com.gdavidpb.tuindice.base.utils.extensions.execute
import com.gdavidpb.tuindice.login.domain.model.SignInRequest
import com.gdavidpb.tuindice.login.domain.usecase.ReSignInUseCase
import com.gdavidpb.tuindice.login.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInError

class SignInViewModel(
	private val signInUseCase: SignInUseCase,
	private val reSignInUseCase: ReSignInUseCase
) : ViewModel() {
	val signIn = LiveEvent<Unit, SignInError>()

	fun signIn(usbId: String, password: String) =
		execute(useCase = signInUseCase, params = SignInRequest(usbId, password), liveData = signIn)

	fun reSignIn(password: String) =
		execute(useCase = reSignInUseCase, params = password, liveData = signIn)
}