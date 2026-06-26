package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.domain.usecase.base.InitialContentFreshness
import com.gdavidpb.tuindice.base.domain.usecase.base.InitialContentLoadResult
import com.gdavidpb.tuindice.base.domain.usecase.base.InitialContentRefreshPolicy
import com.gdavidpb.tuindice.base.domain.usecase.base.ensureInitialContentLoaded
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationsRefreshResult
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EnsureEvaluationsLoadedUseCase(
	private val evaluationRepository: EvaluationRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, EnsureEvaluationsLoadedUseCase.Result, Nothing>() {
	sealed interface Result {
		data object Cached : Result
		data object RefreshStarted : Result
		data class RefreshSucceeded(
			val refreshResult: EvaluationsRefreshResult
		) : Result
	}

	override suspend fun executeOnBackground(params: Unit): Flow<Result> {
		var refreshResult: EvaluationsRefreshResult? = null
		return ensureInitialContentLoaded(
			hasLocalContent = { evaluationRepository.getEvaluationsSnapshot().value.isNotEmpty() },
			refreshPolicy = InitialContentRefreshPolicy.Always,
			refresh = { request ->
				refreshResult = evaluationRepository.updateEvaluations(
					forceRemote = request.freshness == InitialContentFreshness.ForceRemote
				)
			}
		).map { result ->
			when (result) {
				InitialContentLoadResult.Cached -> Result.Cached
				InitialContentLoadResult.RefreshStarted -> Result.RefreshStarted
				InitialContentLoadResult.RefreshSucceeded -> Result.RefreshSucceeded(
					refreshResult = checkNotNull(refreshResult)
				)
			}
		}
	}
}
