package $PACKAGE.presentation.action

import $PACKAGE.domain.usecase.$OBSERVE_USE_CASE_NAME
import $PACKAGE.presentation.contract.$FEATURE_NAME
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.jetbrains.compose.resources.getString
import $GENERATED_RESOURCES_PACKAGE.Res

class $OBSERVE_ACTION_PROCESSOR_NAME(
	private val $OBSERVE_USE_CASE_PARAM_NAME: $OBSERVE_USE_CASE_NAME
) : ActionProcessor<$FEATURE_NAME.State, $FEATURE_NAME.Action.$OBSERVE_ACTION_NAME, $FEATURE_NAME.Effect> {

	override suspend fun process(
		action: $FEATURE_NAME.Action.$OBSERVE_ACTION_NAME,
		sideEffect: ($FEATURE_NAME.Effect) -> Unit
	): Flow<Mutation<$FEATURE_NAME.State>> {
		return $OBSERVE_USE_CASE_PARAM_NAME.execute(Unit)
			.mapNotNull { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> null

					is UseCaseState.Data -> suspend { _: $FEATURE_NAME.State ->
						$FEATURE_NAME.State.Content(
							message = useCaseState.value
						)
					}

					is UseCaseState.Error -> suspend { _: $FEATURE_NAME.State ->
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
