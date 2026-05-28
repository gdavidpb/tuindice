package $PACKAGE.presentation.action

import $PACKAGE.domain.usecase.$UPDATE_USE_CASE_NAME
import $PACKAGE.domain.usecase.error.$UPDATE_USE_CASE_ERROR_NAME
import $PACKAGE.presentation.contract.$FEATURE_NAME
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.jetbrains.compose.resources.getString
import $GENERATED_RESOURCES_PACKAGE.Res

class $REFRESH_ACTION_PROCESSOR_NAME(
	private val $UPDATE_USE_CASE_PARAM_NAME: $UPDATE_USE_CASE_NAME
) : ActionProcessor<$FEATURE_NAME.State, $FEATURE_NAME.Action.$REFRESH_ACTION_NAME, $FEATURE_NAME.Effect>() {

	override suspend fun process(
		action: $FEATURE_NAME.Action.$REFRESH_ACTION_NAME,
		sideEffect: ($FEATURE_NAME.Effect) -> Unit
	): Flow<Mutation<$FEATURE_NAME.State>> {
		return $UPDATE_USE_CASE_PARAM_NAME.execute(Unit)
			.mapNotNull { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> suspend { state: $FEATURE_NAME.State ->
						when (state) {
							is $FEATURE_NAME.State.Content -> state
							$FEATURE_NAME.State.Failed,
							$FEATURE_NAME.State.Loading,
							-> $FEATURE_NAME.State.Loading
						}
					}

					is UseCaseState.Data -> null

					is UseCaseState.Error -> suspend { state: $FEATURE_NAME.State ->
						val message = when (val error = useCaseState.error) {
							is $UPDATE_USE_CASE_ERROR_NAME.NoConnection ->
								if (error.isNetworkAvailable)
									getString(Res.string.snack_service_unavailable)
								else
									getString(Res.string.snack_network_unavailable)

							is $UPDATE_USE_CASE_ERROR_NAME.Timeout ->
								getString(Res.string.snack_timeout)

							is $UPDATE_USE_CASE_ERROR_NAME.Unavailable ->
								getString(Res.string.snack_service_unavailable)

							else ->
								getString(Res.string.snack_default_error)
						}

						sideEffect($FEATURE_NAME.Effect.ShowSnackBar(message))

						when (state) {
							is $FEATURE_NAME.State.Content -> state
							$FEATURE_NAME.State.Failed,
							$FEATURE_NAME.State.Loading,
							-> $FEATURE_NAME.State.Failed
						}
					}
				}
			}
	}
}
