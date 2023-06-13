package com.gdavidpb.tuindice.base.utils.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

@Composable
inline fun <reified T> CollectEffectWithLifecycle(
	flow: Flow<T>,
	lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
	minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
	noinline action: suspend (T) -> Unit
) {
	LaunchedEffect(Unit) {
		flow
			.flowWithLifecycle(lifecycleOwner.lifecycle, minActiveState)
			.collect(action)
	}
}

fun <T> CoroutineScope.collect(
	flow: Flow<T>,
	collector: FlowCollector<T>
) = launch { flow.collect(collector) }

suspend inline fun <reified State : ViewState, reified Event : ViewEvent, T, E> Flow<UseCaseState<T, E>>.collect(
	viewModel: BaseViewModel<State, *, Event>,
	reducer: BaseReducer<State, Event, T, E>
) {
	reducer
		.reduce(useCaseFlow = this, stateProvider = viewModel::getCurrentState)
		.collect { output ->
			when (output) {
				is State -> viewModel.setState(output)
				is Event -> viewModel.sendEvent(output)
			}
		}
}