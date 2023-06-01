package com.gdavidpb.tuindice.login.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.login.R
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInError
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class UpdatePasswordReducer(
	private val resourceResolver: ResourceResolver
) : BaseReducer<UpdatePassword.State, UpdatePassword.Event, Unit, SignInError>() {

	override suspend fun reduceLoadingState(
		currentState: UpdatePassword.State,
		useCaseState: UseCaseState.Loading<Unit, SignInError>
	): Flow<ViewOutput> {
		return if (currentState is UpdatePassword.State.Idle)
			flowOf(
				UpdatePassword.State.Updating(
					password = currentState.password
				)
			)
		else
			super.reduceLoadingState(currentState, useCaseState)
	}

	override suspend fun reduceDataState(
		currentState: UpdatePassword.State,
		useCaseState: UseCaseState.Data<Unit, SignInError>
	): Flow<ViewOutput> {
		return flow {
			emit(
				UpdatePassword.Event.ShowSnackBar(
					message = resourceResolver.getString(R.string.snack_password_updated)
				)
			)

			emit(
				UpdatePassword.Event.CloseDialog
			)
		}
	}

	override suspend fun reduceErrorState(
		currentState: UpdatePassword.State,
		useCaseState: UseCaseState.Error<Unit, SignInError>
	): Flow<ViewOutput> {
		return if (currentState is UpdatePassword.State.Updating)
			flow {
				val error = when (val error = useCaseState.error) {
					is SignInError.InvalidCredentials ->
						resourceResolver.getString(R.string.error_invalid_password)

					is SignInError.NoConnection ->
						if (error.isNetworkAvailable)
							resourceResolver.getString(R.string.snack_service_unavailable)
						else
							resourceResolver.getString(R.string.snack_network_unavailable)

					is SignInError.Timeout ->
						resourceResolver.getString(R.string.snack_timeout)

					is SignInError.Unavailable ->
						resourceResolver.getString(R.string.snack_service_unavailable)

					else ->
						resourceResolver.getString(R.string.snack_default_error)
				}

				emit(
					UpdatePassword.State.Idle(
						password = currentState.password,
						error = error
					)
				)
			}
		else
			super.reduceErrorState(currentState, useCaseState)
	}
}