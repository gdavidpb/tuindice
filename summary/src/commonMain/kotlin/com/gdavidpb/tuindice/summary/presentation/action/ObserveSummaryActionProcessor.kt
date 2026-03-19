package com.gdavidpb.tuindice.summary.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveUserUseCase
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.mapper.formatLastUpdate
import com.gdavidpb.tuindice.summary.presentation.mapper.toShortName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.jetbrains.compose.resources.getString
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.snack_default_error
import tuindice.summary.generated.resources.text_sync_healthy

class ObserveSummaryActionProcessor(
	private val observeUserUseCase: ObserveUserUseCase
) : ActionProcessor<Summary.State, Summary.Action.ObserveSummary, Summary.Effect>() {

	override suspend fun process(
		action: Summary.Action.ObserveSummary,
		sideEffect: (Summary.Effect) -> Unit
	): Flow<Mutation<Summary.State>> {
		return observeUserUseCase.execute(Unit).mapNotNull { useCaseState ->
			when (useCaseState) {
				is UseCaseState.Loading -> null

				is UseCaseState.Data -> suspend { _: Summary.State ->
					val user = useCaseState.value
					val lastUpdateText = getString(
						Res.string.text_sync_healthy,
						user.lastUpdate.formatLastUpdate()
					)

					with(user) {
						Summary.State.Content(
							name = toShortName(),
							lastUpdate = lastUpdateText,
							careerName = careerName,
							grade = grade.toFloat(),
							enrolledSubjects = enrolledSubjects,
							enrolledCredits = enrolledCredits,
							approvedSubjects = approvedSubjects,
							approvedCredits = approvedCredits,
							retiredSubjects = retiredSubjects,
							retiredCredits = retiredCredits,
							failedSubjects = failedSubjects,
							failedCredits = failedCredits,
							profilePictureUrl = pictureUrl,
							isGradeVisible = (grade > 0.0),
							isProfilePictureLoading = false
						)
					}
				}

				is UseCaseState.Error -> suspend { _: Summary.State ->
					val message = getString(Res.string.snack_default_error)

					sideEffect(
						Summary.Effect.ShowSnackBar(
							message = message
						)
					)

					Summary.State.Failed
				}
			}
		}
	}
}
