package com.gdavidpb.tuindice.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.presentation.contract.Main
import com.google.android.play.core.review.ReviewInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class RequestReviewReducer : BaseReducer<Main.State, Main.Event, ReviewInfo, Nothing>() {
	override suspend fun reduceDataState(
		currentState: Main.State,
		useCaseState: UseCaseState.Data<ReviewInfo, Nothing>
	): Flow<ViewOutput> {
		return flowOf(Main.Event.ShowReviewDialog(reviewInfo = useCaseState.value))
	}
}