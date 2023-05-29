package com.gdavidpb.tuindice.summary.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.summary.R
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureError
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class TakeProfilePictureReducer(
	private val resourceResolver: ResourceResolver
) : BaseReducer<Summary.State, Summary.Event, String, ProfilePictureError>() {

	override fun reduceUnrecoverableState(
		currentState: Summary.State,
		throwable: Throwable
	): Flow<ViewOutput> {
		return flowOf(
			Summary.Event.ShowSnackBar(
				message = resourceResolver.getString(R.string.snack_default_error)
			)
		)
	}

	override suspend fun reduceDataState(
		currentState: Summary.State,
		useCaseState: UseCaseState.Data<String, ProfilePictureError>
	): Flow<ViewOutput> {
		return flowOf(Summary.Event.OpenCamera(output = useCaseState.value))
	}

	override suspend fun reduceErrorState(
		currentState: Summary.State,
		useCaseState: UseCaseState.Error<String, ProfilePictureError>
	): Flow<ViewOutput> {
		return flowOf(
			Summary.Event.ShowSnackBar(
				message = resourceResolver.getString(R.string.snack_default_error)
			)
		)
	}
}