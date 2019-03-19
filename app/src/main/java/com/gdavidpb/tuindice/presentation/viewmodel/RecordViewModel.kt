package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.domain.usecase.GetQuartersUseCase
import com.gdavidpb.tuindice.domain.usecase.UpdateSubjectUseCase
import com.gdavidpb.tuindice.utils.LiveCompletable
import com.gdavidpb.tuindice.utils.LiveResult

open class RecordViewModel(
        private val getQuartersUseCase: GetQuartersUseCase,
        private val updateSubjectUseCase: UpdateSubjectUseCase
) : ViewModel() {
    val quarters = LiveResult<List<Quarter>>()
    val update = LiveCompletable()

    fun getQuarters() {
        getQuartersUseCase.execute(liveData = quarters, params = Unit)
    }

    fun updateSubject(subject: Subject) {
        updateSubjectUseCase.execute(liveData = update, params = subject)
    }
}