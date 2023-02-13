package com.gdavidpb.tuindice.base.utils.extension

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

fun <T> emptyStateFlow() = MutableStateFlow<T?>(null)

fun <T> CoroutineScope.collect(
	flow: Flow<T>,
	collector: FlowCollector<T>
) = launch { flow.collect(collector) }

fun LifecycleOwner.launchRepeatOnLifecycle(
	state: Lifecycle.State = Lifecycle.State.STARTED,
	block: suspend CoroutineScope.() -> Unit
) {
	val (lifecycleScope, lifecycle) = when (this) {
		is Fragment -> viewLifecycleOwner.lifecycleScope to viewLifecycleOwner.lifecycle
		is Activity -> lifecycleScope to lifecycle
		else -> throw NoWhenBranchMatchedException()
	}

	lifecycleScope.launch {
		lifecycle.repeatOnLifecycle(state) {
			block()
		}
	}
}

fun <T> ViewModel.emit(flow: MutableStateFlow<T>, value: T) {
	viewModelScope.launch { flow.emit(value) }
}

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