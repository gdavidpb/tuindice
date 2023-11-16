package com.gdavidpb.tuindice.presentation.action.main

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.domain.usecase.RequestReviewUseCase
import com.gdavidpb.tuindice.presentation.contract.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RequestReviewActionProcessor(
	private val requestReviewUseCase: RequestReviewUseCase
) : ActionProcessor<Main.State, Main.Action.RequestReview, Main.Effect>() {

	override fun process(
		action: Main.Action.RequestReview,
		sideEffect: (Main.Effect) -> Unit
	): Flow<Mutation<Main.State>> {
		return requestReviewUseCase.execute(params = action.reviewManager)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> { state ->
						state
					}

					is UseCaseState.Data -> { state ->
						sideEffect(
							Main.Effect.ShowReviewDialog(reviewInfo = useCaseState.value)
						)

						state
					}

					is UseCaseState.Error -> { state ->
						state
					}
				}
			}
	}
}