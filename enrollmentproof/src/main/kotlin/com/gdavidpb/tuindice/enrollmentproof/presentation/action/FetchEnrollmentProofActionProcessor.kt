package com.gdavidpb.tuindice.enrollmentproof.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.enrollmentproof.R
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.FetchEnrollmentProofUseCase
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.error.FetchEnrollmentProofUseCaseError
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FetchEnrollmentProofActionProcessor(
	private val enrollmentProofUseCase: FetchEnrollmentProofUseCase,
	private val resourceResolver: ResourceResolver
) : ActionProcessor<Enrollment.State, Enrollment.Action.FetchEnrollmentProof, Enrollment.Effect>() {

	override fun process(
		action: Enrollment.Action.FetchEnrollmentProof,
		sideEffect: (Enrollment.Effect) -> Unit
	): Flow<Mutation<Enrollment.State>> {
		return enrollmentProofUseCase.execute(Unit)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> { _ ->
						Enrollment.State.Fetching
					}

					is UseCaseState.Data -> { state ->
						sideEffect(
							Enrollment.Effect.OpenEnrollmentProof(path = useCaseState.value)
						)

						state
					}

					is UseCaseState.Error -> { state ->
						when (val error = useCaseState.error) {
							is FetchEnrollmentProofUseCaseError.NoConnection ->
								sideEffect(
									Enrollment.Effect.ShowSnackBar(
										message = if (error.isNetworkAvailable)
											resourceResolver.getString(R.string.snack_service_unavailable)
										else
											resourceResolver.getString(R.string.snack_network_unavailable)
									)
								)

							is FetchEnrollmentProofUseCaseError.NoContent ->
								sideEffect(
									Enrollment.Effect.ShowSnackBar(
										message = resourceResolver.getString(R.string.snack_enrollment_not_found)
									)
								)

							is FetchEnrollmentProofUseCaseError.UnsupportedFile ->
								sideEffect(
									Enrollment.Effect.ShowSnackBar(
										message = resourceResolver.getString(R.string.snack_enrollment_unsupported)
									)
								)

							is FetchEnrollmentProofUseCaseError.OutdatedPassword ->
								sideEffect(
									Enrollment.Effect.NavigateToOutdatedPassword
								)

							is FetchEnrollmentProofUseCaseError.Timeout ->
								sideEffect(
									Enrollment.Effect.ShowSnackBar(
										message = resourceResolver.getString(R.string.snack_timeout)
									)
								)

							is FetchEnrollmentProofUseCaseError.Unavailable ->
								sideEffect(
									Enrollment.Effect.ShowSnackBar(
										message = resourceResolver.getString(R.string.snack_service_unavailable)
									)
								)

							else ->
								sideEffect(
									Enrollment.Effect.ShowSnackBar(
										message = resourceResolver.getString(R.string.snack_default_error)
									)
								)
						}

						state
					}
				}
			}
	}
}