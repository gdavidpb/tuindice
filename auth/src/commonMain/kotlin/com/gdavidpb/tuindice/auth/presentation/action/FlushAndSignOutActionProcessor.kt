package com.gdavidpb.tuindice.auth.presentation.action

import com.gdavidpb.tuindice.auth.domain.usecase.FlushPendingChangesUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.base.domain.model.FlushPendingChangesResult
import com.gdavidpb.tuindice.auth.presentation.contract.SignOut
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jetbrains.compose.resources.getString
import tuindice.auth.generated.resources.Res
import tuindice.auth.generated.resources.snack_default_error

class FlushAndSignOutActionProcessor(
	private val flushPendingChangesUseCase: FlushPendingChangesUseCase,
	private val signOutUseCase: SignOutUseCase
) : ActionProcessor<SignOut.State, SignOut.Action.FlushAndSignOut, SignOut.Effect> {
	override suspend fun process(
		action: SignOut.Action.FlushAndSignOut,
		sideEffect: (SignOut.Effect) -> Unit
	): Flow<Mutation<SignOut.State>> {
		return flow {
			flushPendingChangesUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading ->
						emit(
							suspend { _: SignOut.State ->
								SignOut.State.LoggingOut(pendingChanges = action.pendingChanges)
							}
						)

					is UseCaseState.Data ->
						when (val result = useCaseState.value) {
							FlushPendingChangesResult.Success ->
								signOutUseCase.execute(Unit).collect { signOutState ->
									when (signOutState) {
										is UseCaseState.Loading ->
											emit(
												suspend { _: SignOut.State ->
													SignOut.State.LoggingOut(pendingChanges = action.pendingChanges)
												}
											)

										is UseCaseState.Data ->
											emit(
												suspend { state ->
													sideEffect(SignOut.Effect.NavigateToSignIn)
													state
												}
											)

										is UseCaseState.Error ->
											emit(
												suspend { _: SignOut.State ->
													sideEffect(
														SignOut.Effect.ShowSnackBar(
															message = getString(Res.string.snack_default_error)
														)
													)
													SignOut.State.FlushFailed(
														pendingChanges = action.pendingChanges,
														requiresPasswordUpdate = false
													)
												}
											)
									}
								}

							is FlushPendingChangesResult.PendingRemaining ->
								emit(
									suspend { _: SignOut.State ->
										SignOut.State.FlushFailed(
											pendingChanges = result.pendingChanges,
											requiresPasswordUpdate = false
										)
									}
								)

							is FlushPendingChangesResult.OutdatedCredentials ->
								emit(
									suspend { _: SignOut.State ->
										SignOut.State.FlushFailed(
											pendingChanges = result.pendingChanges,
											requiresPasswordUpdate = true
										)
									}
								)
						}

					is UseCaseState.Error ->
						emit(
							suspend { _: SignOut.State ->
								sideEffect(
									SignOut.Effect.ShowSnackBar(
										message = getString(Res.string.snack_default_error)
									)
								)
								SignOut.State.FlushFailed(
									pendingChanges = action.pendingChanges,
									requiresPasswordUpdate = false
								)
							}
						)
				}
			}
		}
	}
}
