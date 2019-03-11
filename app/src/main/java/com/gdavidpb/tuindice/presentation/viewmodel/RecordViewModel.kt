package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.usecase.GetQuartersUseCase
import com.gdavidpb.tuindice.utils.LiveResult

open class RecordViewModel(
        private val getQuartersUseCase: GetQuartersUseCase
) : ViewModel() {
    val quarters = LiveResult<List<Quarter>>()

    fun getQuarters() {
        getQuartersUseCase.execute(liveData = quarters, params = Unit)
    }
}