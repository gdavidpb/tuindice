package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.summary.domain.model.ShareTermSource
import com.gdavidpb.tuindice.summary.domain.repository.ShareBackgroundRepository
import com.gdavidpb.tuindice.summary.domain.repository.ShareBackgroundSelectionRepository
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

/**
 * What the share sheet of a closed term reads: the closed terms of the hub's statistics, the career of the student, the
 * backgrounds as they were last saved and the one chosen last. Everything is read from local state. The sheet asks the
 * server for the catalog through [UpdateShareBackgroundsUseCase] when it opens, and the backgrounds follow once it
 * lands. The hub is observed through its own use case, so the sheet follows the clock as the Statistics screen does.
 */
class ObserveShareTermUseCase(
	private val observeSummaryHubUseCase: ObserveSummaryHubUseCase,
	private val userRepository: UserRepository,
	private val shareBackgroundRepository: ShareBackgroundRepository,
	private val shareBackgroundSelectionRepository: ShareBackgroundSelectionRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, ShareTermSource, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<ShareTermSource> {
		val closedTerms = observeSummaryHubUseCase.execute(Unit).mapNotNull { useCaseState ->
			when (useCaseState) {
				is UseCaseState.Loading -> null

				is UseCaseState.Data -> useCaseState.value.statistics?.closed?.terms.orEmpty()

				// The hub could not be read: the failure goes on to the caller, which reports the terms as unavailable.
				is UseCaseState.Error -> throw IllegalStateException(HUB_UNAVAILABLE)
			}
		}
		val careerNames = userRepository.observeUserFlow().map { user -> user.careerName }

		return combine(
			closedTerms,
			careerNames,
			shareBackgroundRepository.observeShareBackgrounds(),
			shareBackgroundSelectionRepository.observePreferredBackgroundId()
		) { terms, careerName, backgrounds, preferredBackgroundId ->
			ShareTermSource(
				closedTerms = terms,
				careerName = careerName,
				backgrounds = backgrounds,
				preferredBackgroundId = preferredBackgroundId
			)
		}
	}

	private companion object {
		const val HUB_UNAVAILABLE = "The statistics of the record could not be observed."
	}
}
