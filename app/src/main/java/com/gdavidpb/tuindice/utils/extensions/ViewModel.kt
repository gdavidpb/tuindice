package com.gdavidpb.tuindice.utils.extensions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdavidpb.tuindice.domain.usecase.coroutines.BaseUseCase

fun <P, T, Q, L, U : BaseUseCase<P, T, Q, L>> ViewModel.execute(useCase: U, params: P, liveData: L) =
        useCase.execute(params, liveData, viewModelScope)