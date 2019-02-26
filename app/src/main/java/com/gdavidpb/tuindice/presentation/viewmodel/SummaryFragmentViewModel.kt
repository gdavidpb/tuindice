package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.utils.LiveResult
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.usecase.SyncAccountUseCase

open class SummaryFragmentViewModel(
        private val syncAccountUseCase: SyncAccountUseCase
) : ViewModel() {

    val loadAccount = LiveResult<Account>()

    fun loadAccount(trySync: Boolean) {
        syncAccountUseCase.execute(liveData = loadAccount, params = trySync)
    }
}