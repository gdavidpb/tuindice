package com.gdavidpb.tuindice.pensum.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.pensum.domain.usecase.UpdatePensumUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.error.UpdatePensumUseCaseError
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_failed_message
import tuindice.pensum.generated.resources.pensum_failed_network_unavailable
import tuindice.pensum.generated.resources.pensum_failed_service_unavailable
import tuindice.pensum.generated.resources.pensum_failed_timeout
import tuindice.pensum.generated.resources.snack_default_error
import tuindice.pensum.generated.resources.snack_network_unavailable
import tuindice.pensum.generated.resources.snack_service_unavailable
import tuindice.pensum.generated.resources.snack_timeout

class RefreshPensumActionProcessor(
	private val updatePensumUseCase: UpdatePensumUseCase
) : ActionProcessor<Pensum.State, Pensum.Action.RefreshPensum, Pensum.Effect>() {
	override suspend fun process(
		action: Pensum.Action.RefreshPensum,
		sideEffect: (Pensum.Effect) -> Unit
	): Flow<Mutation<Pensum.State>> {
		return updatePensumUseCase.execute(Unit)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> suspend { state: Pensum.State -> state.loadingOrContent() }
					is UseCaseState.Data -> suspend { state: Pensum.State -> state.idleOrContent() }
					is UseCaseState.Error -> suspend { state: Pensum.State ->
						if (useCaseState.error == UpdatePensumUseCaseError.NotFound) {
							Pensum.State.Empty
						} else {
							state.snackBarEffectOrNull(useCaseState.error)?.let(sideEffect)
							state.failedOrContent(useCaseState.error.toFailedMessage())
						}
					}
				}
			}
	}

	private fun Pensum.State.loadingOrContent(): Pensum.State = when (this) {
		is Pensum.State.Content -> copy(isRefreshing = true)
		Pensum.State.Empty,
		is Pensum.State.Failed,
		Pensum.State.Idle,
		Pensum.State.Loading,
		-> Pensum.State.Loading
	}

	private fun Pensum.State.idleOrContent(): Pensum.State = when (this) {
		is Pensum.State.Content -> copy(isRefreshing = false)
		Pensum.State.Empty,
		is Pensum.State.Failed,
		Pensum.State.Idle,
		Pensum.State.Loading,
		-> this
	}

	private fun Pensum.State.failedOrContent(message: UiText): Pensum.State = when (this) {
		is Pensum.State.Content -> copy(isRefreshing = false)
		Pensum.State.Empty,
		is Pensum.State.Failed,
		Pensum.State.Idle,
		Pensum.State.Loading,
		-> Pensum.State.Failed(message = message)
	}

	private fun Pensum.State.snackBarEffectOrNull(
		error: UpdatePensumUseCaseError?
	): Pensum.Effect.ShowSnackBar? {
		return if (this is Pensum.State.Content && error.shouldNotifyVisibleContent()) {
			Pensum.Effect.ShowSnackBar(error.toSnackBarMessage())
		} else {
			null
		}
	}

	private fun UpdatePensumUseCaseError?.shouldNotifyVisibleContent(): Boolean {
		return when (this) {
			is UpdatePensumUseCaseError.NoConnection -> isNetworkAvailable
			UpdatePensumUseCaseError.NotFound,
			UpdatePensumUseCaseError.Timeout,
			UpdatePensumUseCaseError.Unavailable,
			null,
			-> true
		}
	}

	private fun UpdatePensumUseCaseError?.toSnackBarMessage(): UiText {
		return when (this) {
			UpdatePensumUseCaseError.NotFound ->
				UiText.Resource(Res.string.snack_default_error)

			is UpdatePensumUseCaseError.NoConnection ->
				if (isNetworkAvailable)
					UiText.Resource(Res.string.snack_service_unavailable)
				else
					UiText.Resource(Res.string.snack_network_unavailable)

			UpdatePensumUseCaseError.Timeout ->
				UiText.Resource(Res.string.snack_timeout)

			UpdatePensumUseCaseError.Unavailable ->
				UiText.Resource(Res.string.snack_service_unavailable)

			null ->
				UiText.Resource(Res.string.snack_default_error)
		}
	}

	private fun UpdatePensumUseCaseError?.toFailedMessage(): UiText {
		return when (this) {
			UpdatePensumUseCaseError.NotFound ->
				UiText.Resource(Res.string.pensum_failed_message)

			is UpdatePensumUseCaseError.NoConnection ->
				if (isNetworkAvailable)
					UiText.Resource(Res.string.pensum_failed_service_unavailable)
				else
					UiText.Resource(Res.string.pensum_failed_network_unavailable)

			UpdatePensumUseCaseError.Timeout ->
				UiText.Resource(Res.string.pensum_failed_timeout)

			UpdatePensumUseCaseError.Unavailable ->
				UiText.Resource(Res.string.pensum_failed_service_unavailable)

			null ->
				UiText.Resource(Res.string.pensum_failed_message)
		}
	}
}
