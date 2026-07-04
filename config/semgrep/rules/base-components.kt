// Fixture de semgrep --test para base-components.yaml.
// No es código del proyecto: cada línea anotada valida una regla.
// La exención de *Draft.kt (machine-state-only-in-draft) y las exclusiones de
// base/** son por paths y se validan en la prueba de generalidad con módulo
// sintético (en --test los paths no aplican).
package com.gdavidpb.tuindice.sample.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

// ruleid: usecase-extends-flowusecase
class RogueThingUseCase(
	private val otherUseCase: GoodThingUseCase
) {
	suspend fun run(): String {
		// ruleid: usecase-no-trycatch
		try {
			// ruleid: no-direct-executeonbackground
			return otherUseCase.executeOnBackground(Unit).toString()
		} catch (exception: Exception) {
			// ruleid: no-usecasestate-outside-base
			val fabricated = UseCaseState.Error(null)
			return fabricated.toString()
		}
	}
}

// ok: usecase-extends-flowusecase
class GoodThingUseCase(
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, String, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<String> {
		// ok: no-direct-executeonbackground
		return flowOf("ok")
	}
}

fun describe(state: UseCaseState<String, Nothing>): String = when (state) {
	// ok: no-usecasestate-outside-base
	is UseCaseState.Data -> "data"
	else -> "other"
}

fun publishRogueEvent(publish: (Any) -> Unit) {
	// ruleid: no-appevent-outside-base
	publish(AppEvent.ScreenView(source = "sample"))
}

// ruleid: machine-implements-screenmachine
class RogueMachine(
	private val loadUseCase: GoodThingUseCase
) {
	// ruleid: machine-state-only-in-draft
	private val query = MutableStateFlow("")

	fun initialState(): Any = Unit
}

// ok: machine-implements-screenmachine
class GoodMachine(
	private val loadUseCase: GoodThingUseCase
) : ScreenMachine<Any, Any> {
	override fun initialState(): Any = Unit

	override fun define(host: Any): Any = Unit
}
