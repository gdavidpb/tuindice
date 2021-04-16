package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.usecase.UpdatePasswordUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.UpdatePasswordError
import com.gdavidpb.tuindice.utils.extensions.LiveCompletable
import com.gdavidpb.tuindice.utils.extensions.execute

class UpdatePasswordViewModel(
        private val updatePasswordUseCase: UpdatePasswordUseCase
) : ViewModel() {
    val updatePassword = LiveCompletable<UpdatePasswordError>()

    fun updatePassword(password: String) =
            execute(useCase = updatePasswordUseCase, params = password, liveData = updatePassword)
}