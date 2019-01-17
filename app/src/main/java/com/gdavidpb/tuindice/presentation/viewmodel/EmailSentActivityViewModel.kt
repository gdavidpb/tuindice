package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.data.utils.LiveResult
import com.gdavidpb.tuindice.domain.usecase.GetEmailSentToUseCase

open class EmailSentActivityViewModel(
        private val getEmailSentToUseCase: GetEmailSentToUseCase
) : ViewModel() {

    val getEmailSentTo = LiveResult<String>()

    fun getEmailSentTo() {
        getEmailSentToUseCase.execute(liveData = getEmailSentTo, params = Unit)
    }
}