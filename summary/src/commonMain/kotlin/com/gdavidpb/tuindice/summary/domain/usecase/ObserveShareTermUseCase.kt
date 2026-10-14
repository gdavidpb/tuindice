package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.summary.domain.model.ShareTermSource
import com.gdavidpb.tuindice.summary.domain.repository.ShareBackgroundRepository
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

/**
 * What the share sheet of a closed term reads: the closed terms of the hub's statistics, the career of the student, and
 * the backgrounds with the one chosen last. The hub is observed through its own use case, so the sheet follows the
 * clock as the Statistics screen does.
 */
class ObserveShareTermUseCase(
	private val observeSummaryHubUseCase: ObserveSummaryHubUseCase,
	private val userRepository: UserRepository,
	private val shareBackgroundRepository: ShareBackgroundRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, ShareTermSource, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<ShareTermSource> {
		// The catalog never fails: it serves the last list it read, and the app's own background is always in it.
		val backgrounds = shareBackgroundRepository.getShareBackgrounds()
		val preferredBackgroundId = shareBackgroundRepository.getPreferredBackgroundId()
		val closedTerms = observeSummaryHubUseCase.execute(Unit).mapNotNull { useCaseState ->
			when (useCaseState) {
				is UseCaseState.Loading -> null

				is UseCaseState.Data -> useCaseState.value.statistics?.closed?.terms.orEmpty()

				// The hub could not be read: the failure goes on to the caller, which reports the terms as unavailable.
				is UseCaseState.Error -> throw IllegalStateException(HUB_UNAVAILABLE)
			}
		}
		val careerNames = userRepository.observeUserFlow().map { user -> user.careerName }

		return combine(closedTerms, careerNames) { terms, careerName ->
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
