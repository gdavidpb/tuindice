package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.usecase.SyncAccountUseCase
import com.gdavidpb.tuindice.utils.LiveContinuous
import com.gdavidpb.tuindice.utils.execute

class SummaryViewModel(
        private val syncAccountUseCase: SyncAccountUseCase
) : ViewModel() {
    val account = LiveContinuous<Account>()

    fun loadAccount(trySync: Boolean) =
            execute(useCase = syncAccountUseCase, params = trySync, liveData = account)
}