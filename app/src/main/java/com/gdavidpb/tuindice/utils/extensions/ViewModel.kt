package com.gdavidpb.tuindice.utils.extensions

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdavidpb.tuindice.domain.usecase.coroutines.BaseUseCase

fun <T : BaseUseCase<Q, W>, Q, W : MutableLiveData<*>> ViewModel.execute(useCase: T, params: Q, liveData: W) {
    useCase.execute(params, liveData, viewModelScope)
}