package com.gdavidpb.tuindice.login.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.domain.usecase.SyncUseCase
import com.gdavidpb.tuindice.base.domain.usecase.errors.SyncError
import com.gdavidpb.tuindice.base.utils.extensions.LiveCompletable
import com.gdavidpb.tuindice.base.utils.extensions.LiveEvent
import com.gdavidpb.tuindice.base.utils.extensions.LiveResult
import com.gdavidpb.tuindice.base.domain.model.SignInRequest
import com.gdavidpb.tuindice.base.utils.extensions.execute
import com.gdavidpb.tuindice.login.domain.usecase.ReSignInUseCase
import com.gdavidpb.tuindice.login.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.base.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.login.domain.usecase.errors.SignInError

class SignInViewModel(
	private val signInUseCase: SignInUseCase,
	private val reSignInUseCase: ReSignInUseCase,
	private val signOutUseCase: SignOutUseCase,
	private val syncUseCase: SyncUseCase
) : ViewModel() {
	val signIn = LiveEvent<Boolean, SignInError>()
	val signOut = LiveCompletable<Nothing>()
	val sync = LiveResult<Boolean, SyncError>()

	fun signIn(usbId: String, password: String) {
		val credentials = SignInRequest(usbId = usbId, password = password)

		execute(useCase = signInUseCase, params = credentials, liveData = signIn)
	}

	fun reSignIn(password: String) =
		execute(useCase = reSignInUseCase, params = password, liveData = signIn)

	fun signOut() =
		execute(useCase = signOutUseCase, params = Unit, liveData = signOut)

	fun sync() =
		execute(useCase = syncUseCase, params = Unit, liveData = sync)
}