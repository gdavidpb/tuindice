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

// usecase-collaborators-injected: el colaborador se crea dentro del caso de uso (ruleid) o se recibe (ok).
class CreatesItsCollaboratorsUseCase(
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Int, Nothing>() {
	// ruleid: usecase-collaborators-injected
	override val paramsValidator: ParamsValidator<Unit> = SampleParamsValidator()

	// ruleid: usecase-collaborators-injected
	override val exceptionHandler: ExceptionHandler<Nothing> = object : ExceptionHandler<Nothing> {
		override fun parseException(throwable: Throwable): Nothing? = null
	}

	override suspend fun executeOnBackground(params: Unit): Flow<Int> = flowOf(1)
}

class ReceivesCollaboratorsUseCase(
	override val paramsValidator: ParamsValidator<Unit>,
	handling: SampleHandling
) : FlowUseCase<Unit, Int, Nothing>() {
	// ok: usecase-collaborators-injected
	override val exceptionHandler: ExceptionHandler<Nothing> = handling.exceptionHandler

	override val reportingRepository: ReportingRepository = handling.reportingRepository

	override suspend fun executeOnBackground(params: Unit): Flow<Int> = flowOf(1)
}
