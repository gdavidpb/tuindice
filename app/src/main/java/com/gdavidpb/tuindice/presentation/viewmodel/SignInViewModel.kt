package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.SignInRequest
import com.gdavidpb.tuindice.domain.usecase.ReSignInUseCase
import com.gdavidpb.tuindice.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.domain.usecase.SyncAccountUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.SignInError
import com.gdavidpb.tuindice.domain.usecase.errors.SyncError
import com.gdavidpb.tuindice.utils.extensions.LiveCompletable
import com.gdavidpb.tuindice.utils.extensions.LiveEvent
import com.gdavidpb.tuindice.utils.extensions.LiveResult
import com.gdavidpb.tuindice.utils.extensions.execute

class SignInViewModel(
    private val signInUseCase: SignInUseCase,
    private val reSignInUseCase: ReSignInUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val syncAccountUseCase: SyncAccountUseCase
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

    fun trySyncAccount() =
        execute(useCase = syncAccountUseCase, params = Unit, liveData = sync)
}