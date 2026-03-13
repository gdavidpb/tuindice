package $PACKAGE.presentation.action

import $PACKAGE.domain.usecase.$LOAD_USE_CASE_NAME
import $PACKAGE.domain.usecase.error.$USE_CASE_ERROR_NAME
import $PACKAGE.presentation.contract.$FEATURE_NAME
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.getString
import $GENERATED_RESOURCES_PACKAGE.Res

class $ACTION_PROCESSOR_NAME(
	private val $LOAD_USE_CASE_PARAM_NAME: $LOAD_USE_CASE_NAME
) : ActionProcessor<$FEATURE_NAME.State, $FEATURE_NAME.Action.$LOAD_ACTION_NAME, $FEATURE_NAME.Effect>() {

	override suspend fun process(
		action: $FEATURE_NAME.Action.$LOAD_ACTION_NAME,
		sideEffect: ($FEATURE_NAME.Effect) -> Unit
	): Flow<Mutation<$FEATURE_NAME.State>> {
		return $LOAD_USE_CASE_PARAM_NAME.execute(Unit)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> suspend { _: $FEATURE_NAME.State ->
						$FEATURE_NAME.State.Loading
					}

					is UseCaseState.Data -> suspend { _: $FEATURE_NAME.State ->
						$FEATURE_NAME.State.Content(
							message = useCaseState.value
						)
					}

					is UseCaseState.Error -> when (val error = useCaseState.error) {
						is $USE_CASE_ERROR_NAME.NoConnection -> suspend { _: $FEATURE_NAME.State ->
							val message = if (error.isNetworkAvailable)
								getString(Res.string.snack_service_unavailable)
							else
								getString(Res.string.snack_network_unavailable)

							sideEffect($FEATURE_NAME.Effect.ShowSnackBar(message))
							$FEATURE_NAME.State.Failed
						}

						is $USE_CASE_ERROR_NAME.Timeout -> suspend { _: $FEATURE_NAME.State ->
							sideEffect(
								$FEATURE_NAME.Effect.ShowSnackBar(
									message = getString(Res.string.snack_timeout)
								)
							)

							$FEATURE_NAME.State.Failed
						}

						is $USE_CASE_ERROR_NAME.Unavailable -> suspend { _: $FEATURE_NAME.State ->
							sideEffect(
								$FEATURE_NAME.Effect.ShowSnackBar(
									message = getString(Res.string.snack_service_unavailable)
								)
							)

							$FEATURE_NAME.State.Failed
						}

						else -> suspend { _: $FEATURE_NAME.State ->
							sideEffect(
								$FEATURE_NAME.Effect.ShowSnackBar(
									message = getString(Res.string.snack_default_error)
								)
							)

							$FEATURE_NAME.State.Failed
						}
					}
				}
			}
	}
}
