package com.gdavidpb.tuindice.base.utils.extension

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdavidpb.tuindice.base.domain.usecase.base.BaseUseCase
import com.gdavidpb.tuindice.base.domain.usecase.baseV2.FlowUseCase
import com.gdavidpb.tuindice.base.domain.usecase.baseV2.UseCaseState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

fun <P, T, Q, L, U : BaseUseCase<P, T, Q, L>> ViewModel.execute(
	useCase: U,
	params: P,
	liveData: L
) = useCase.execute(params, liveData, viewModelScope)

fun <P, T, E, U : FlowUseCase<P, T, E>> ViewModel.stateInWhileSubscribed(
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
fun <P, T, E, U : FlowUseCase<P, T, E>> ViewModel.stateInEagerly(
	useCase: U,
	paramsFlow: MutableSharedFlow<P>
) = paramsFlow
	.transformLatest { params ->
		emitAll(useCase.execute(params))
	}
	.onCompletion {
		paramsFlow.resetReplayCache()
	}
	.stateIn(
		scope = viewModelScope,
		started = SharingStarted.Eagerly,
		initialValue = UseCaseState.Undefined()
	)
