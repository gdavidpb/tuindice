package com.gdavidpb.tuindice.enrollmentproof.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.FetchEnrollmentProofUseCase
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.error.FetchEnrollmentProofUseCaseError
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment
import com.gdavidpb.tuindice.enrollmentproof.presentation.resource.EnrollmentProofTextProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FetchEnrollmentProofActionProcessor(
	private val enrollmentProofUseCase: FetchEnrollmentProofUseCase,
	private val textProvider: EnrollmentProofTextProvider
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
							Enrollment.Effect.OpenEnrollmentProof(fileRef = useCaseState.value)
						)

						state
					}

					is UseCaseState.Error -> { state ->
						when (val error = useCaseState.error) {
							is FetchEnrollmentProofUseCaseError.NoConnection ->
								sideEffect(
									Enrollment.Effect.ShowSnackBar(
										message = if (error.isNetworkAvailable)
											textProvider.serviceUnavailable()
										else
											textProvider.networkUnavailable()
									)
								)

							is FetchEnrollmentProofUseCaseError.NotFound ->
								sideEffect(
									Enrollment.Effect.ShowSnackBar(
										message = textProvider.enrollmentNotFound()
									)
								)

							is FetchEnrollmentProofUseCaseError.UnsupportedFile ->
								sideEffect(
									Enrollment.Effect.ShowSnackBar(
										message = textProvider.enrollmentUnsupported()
									)
								)

							is FetchEnrollmentProofUseCaseError.OutdatedPassword ->
								sideEffect(
									Enrollment.Effect.NavigateToOutdatedPassword
								)

							is FetchEnrollmentProofUseCaseError.Timeout ->
								sideEffect(
									Enrollment.Effect.ShowSnackBar(
										message = textProvider.timeout()
									)
								)

							is FetchEnrollmentProofUseCaseError.Unavailable ->
								sideEffect(
									Enrollment.Effect.ShowSnackBar(
										message = textProvider.serviceUnavailable()
									)
								)

							else ->
								sideEffect(
									Enrollment.Effect.ShowSnackBar(
										message = textProvider.defaultError()
									)
								)
						}

						state
					}
				}
			}
	}
}
