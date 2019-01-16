package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.data.utils.LiveResult
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.usecase.GetAccountUseCase

open class EnrollmentFragmentViewModel(
        private val getAccountUseCase: GetAccountUseCase
) : ViewModel() {

    val loadAccount = LiveResult<Account>()

    fun loadAccount(tryRefresh: Boolean) {
        getAccountUseCase.execute(liveData = loadAccount, params = tryRefresh)
    }
}