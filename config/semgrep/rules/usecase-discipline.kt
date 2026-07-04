// Fixture de semgrep --test para usecase-discipline.yaml.
// No es código del proyecto: cada línea anotada valida una regla.
// Las exenciones por paths (archivos *UseCase.kt, validator/, exceptionhandler/,
// base/) se validan en la prueba de generalidad (en --test los paths no aplican).
package com.gdavidpb.tuindice.sample.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MultiOperationUseCase(
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Int, Nothing>() {
	// ok: usecase-single-operation
	override suspend fun executeOnBackground(params: Unit): Flow<Int> = flowOf(1)

	// ruleid: usecase-single-operation
	fun currentValue(): Int {
		return 1
	}

	// ruleid: usecase-single-operation
	suspend fun refreshNow(): Int {
		return 2
	}

	// ok: usecase-single-operation
	private fun helper(): Int {
		return 3
	}
}

// ruleid: paramsvalidator-location
class MisplacedParamsValidator

// ok: paramsvalidator-location
class SampleCommandValidator

// ruleid: exceptionhandler-location
object MisplacedExceptionHandler

// ok: exceptionhandler-location
object SampleErrorMapper
