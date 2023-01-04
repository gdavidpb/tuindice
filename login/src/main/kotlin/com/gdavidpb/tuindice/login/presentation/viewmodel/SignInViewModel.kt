package com.gdavidpb.tuindice.login.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.domain.usecase.SyncUseCase
import com.gdavidpb.tuindice.base.domain.usecase.error.SyncError
import com.gdavidpb.tuindice.base.utils.extensions.LiveCompletable
import com.gdavidpb.tuindice.base.utils.extensions.LiveEvent
import com.gdavidpb.tuindice.base.utils.extensions.LiveResult
import com.gdavidpb.tuindice.login.domain.model.SignInRequest
import com.gdavidpb.tuindice.base.utils.extensions.execute
import com.gdavidpb.tuindice.login.domain.usecase.ReSignInUseCase
import com.gdavidpb.tuindice.login.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.base.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInError

class SignInViewModel(
	private val signInUseCase: SignInUseCase,
	private val reSignInUseCase: ReSignInUseCase,
	private val signOutUseCase: SignOutUseCase,
	private val syncUseCase: SyncUseCase
) : ViewModel() {
	val signIn = LiveEvent<Unit, SignInError>()
	val signOut = LiveCompletable<Nothing>()
	val sync = LiveResult<Boolean, SyncError>()

	fun signIn(request: SignInRequest) =
		execute(useCase = signInUseCase, params = request, liveData = signIn)

	fun reSignIn(password: String) =
		execute(useCase = reSignInUseCase, params = password, liveData = signIn)

	fun signOut() =
		execute(useCase = signOutUseCase, params = Unit, liveData = signOut)

	fun sync() =
		execute(useCase = syncUseCase, params = Unit, liveData = sync)
}