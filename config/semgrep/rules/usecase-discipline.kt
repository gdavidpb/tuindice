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

// Observe*UseCase: los repositorios de refresco y los getters puntuales no entran; los flujos sí.
class ObserveSampleUseCase(
	private val sampleRepository: SampleRepository,
	private val settingsRepository: SampleSettingsRepository
) : FlowUseCase<Unit, String, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<String> {
		// ruleid: observe-usecase-local-only
		sampleRepository.refreshSample()
		// ruleid: observe-usecase-local-only
		val notice = settingsRepository.getSampleNotice()
		// ruleid: observe-usecase-local-only
		val fresh = sampleRepository.fetchSample()
		// ok: observe-usecase-local-only
		return sampleRepository.observeSampleFlow()
	}
}
