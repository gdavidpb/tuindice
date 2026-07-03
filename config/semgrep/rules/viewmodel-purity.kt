// Fixture de semgrep --test para viewmodel-purity.yaml.
// No es código del proyecto: cada línea anotada valida una regla.
package com.gdavidpb.tuindice.sample.presentation.viewmodel

// ruleid: viewmodel-no-business-import
import com.gdavidpb.tuindice.sample.domain.usecase.LoadThingUseCase
// ruleid: viewmodel-no-business-import
import com.gdavidpb.tuindice.sample.data.source.SampleDataSource
// ruleid: viewmodel-no-business-import
import com.gdavidpb.tuindice.sample.domain.repository.SampleRepository
// ok: viewmodel-no-business-import
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
// ok: viewmodel-no-business-import
import com.gdavidpb.tuindice.sample.presentation.machine.SampleMachine
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

// ruleid: viewmodel-extends-statemachine
class BadViewModel : ViewModel() {
	// ruleid: viewmodel-no-state-holding
	private val uiState = MutableStateFlow(0)

	// ruleid: viewmodel-no-state-holding
	private val events = MutableSharedFlow<Int>()

	// ruleid: viewmodel-no-state-holding
	private val effects = Channel<Int>(64)

	fun refresh() {
		// ruleid: viewmodel-no-coroutines
		viewModelScope.launch { }
	}

	fun runJob() {
		// ruleid: viewmodel-no-coroutines
		launchMachineJob { }
	}

	fun submitIfIdle() {
		// ruleid: viewmodel-no-state-reads
		if (state.value == 0) return
	}

	fun closeAction() {
		// ruleid: viewmodel-no-effect-bypass
		sendEffect(Unit)
	}

	fun completeAction() {
		// ruleid: viewmodel-no-effect-bypass
		processInternalEvent(Unit)
	}

	fun retryIfFailed() {
		// ruleid: viewmodel-no-state-reads
		val snapshot = currentState
		println(snapshot)
	}
}

// ok: viewmodel-extends-statemachine
class GoodViewModel(
	override val screenMachine: SampleMachine,
	override val eventPublisher: EventPublisher
) : StateMachineViewModel<Any, Any, Any>(
	name = "sample",
	initialState = Unit
) {
	// ok: viewmodel-no-coroutines
	fun loadAction() = sendAction(Unit)

	// ok: viewmodel-no-state-reads
	fun typeTextAction(text: String) = sendAction(text)
}
