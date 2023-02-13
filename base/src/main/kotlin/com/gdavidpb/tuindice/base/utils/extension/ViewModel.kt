package com.gdavidpb.tuindice.base.utils.extension

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

fun <P, T, E, U : FlowUseCase<P, T, E>> ViewModel.stateInFlow(
	useCase: U,
	params: P
) = useCase
	.execute(params)
	.stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
		initialValue = UseCaseState.Loading()
	)

@OptIn(ExperimentalCoroutinesApi::class)
fun <P, T, E, U : FlowUseCase<P, T, E>> ViewModel.stateInFlow(
	useCase: U,
	paramsFlow: MutableStateFlow<P?>
) = paramsFlow
	.transformLatest { params ->
		if (params != null) emitAll(useCase.execute(params))
	}
	.stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
		initialValue = UseCaseState.Loading()
	)

@OptIn(ExperimentalCoroutinesApi::class)
fun <P, T, E, U : FlowUseCase<P, T, E>> ViewModel.stateInAction(
	useCase: U,
	paramsFlow: MutableStateFlow<P?>
) = paramsFlow
	.transformLatest { params ->
		if (params != null) emitAll(useCase.execute(params))
	}
	.stateIn(
		scope = viewModelScope,
		started = SharingStarted.Eagerly,
		initialValue = UseCaseState.Undefined()
	)