package com.gdavidpb.tuindice.sampleflow.domain.usecase

// Positivo de usecase-collaborators-injected: el caso de uso crea su validador en lugar de recibirlo.
class SampleFlowCollaboratorUseCase(
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Int, Nothing>() {
	override val paramsValidator: ParamsValidator<Unit> = SampleFlowParamsValidator()

	override suspend fun executeOnBackground(params: Unit): Flow<Int> = flowOf(1)
}
