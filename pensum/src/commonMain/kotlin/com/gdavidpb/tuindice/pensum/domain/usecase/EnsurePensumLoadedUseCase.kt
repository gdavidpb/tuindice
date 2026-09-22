package com.gdavidpb.tuindice.pensum.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.domain.usecase.base.InitialContentFreshness
import com.gdavidpb.tuindice.base.domain.usecase.base.InitialContentLoadResult
import com.gdavidpb.tuindice.base.domain.usecase.base.InitialContentRefreshPolicy
import com.gdavidpb.tuindice.base.domain.usecase.base.ensureInitialContentLoaded
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRepository
import com.gdavidpb.tuindice.pensum.domain.usecase.error.UpdatePensumUseCaseError
import com.gdavidpb.tuindice.pensum.domain.usecase.exceptionhandler.UpdatePensumExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EnsurePensumLoadedUseCase(
	private val pensumRepository: PensumRepository,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: UpdatePensumExceptionHandler
) : FlowUseCase<Unit, EnsurePensumLoadedUseCase.Result, UpdatePensumUseCaseError>() {
	sealed interface Result {
		data object Cached : Result
		data object RefreshStarted : Result
		data object RefreshSucceeded : Result
	}

	override suspend fun executeOnBackground(params: Unit): Flow<Result> {
		return ensureInitialContentLoaded(
			hasLocalContent = { pensumRepository.hasSelectedPensumResponse() },
			// Always, not MissingOnly: a cached pensum is revalidated on every entry once it is older
			// than a day (the repository decides), so curation reaches installed apps.
			refreshPolicy = InitialContentRefreshPolicy.Always,
			refresh = { request ->
				pensumRepository.refreshPensum(
					forceRemote = request.freshness == InitialContentFreshness.ForceRemote
				)
			}
		).map { result ->
			when (result) {
				InitialContentLoadResult.Cached -> Result.Cached
				InitialContentLoadResult.RefreshStarted -> Result.RefreshStarted
				InitialContentLoadResult.RefreshSucceeded -> Result.RefreshSucceeded
			}
		}
	}
}
