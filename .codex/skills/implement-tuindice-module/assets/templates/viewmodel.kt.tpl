package $PACKAGE.presentation.viewmodel

import $PACKAGE.presentation.action.$ACTION_PROCESSOR_NAME
import $PACKAGE.presentation.contract.$FEATURE_NAME
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.Flow

class $VIEWMODEL_NAME(
	private val $ACTION_PROCESSOR_PARAM_NAME: $ACTION_PROCESSOR_NAME
) : BaseViewModel<$FEATURE_NAME.State, $FEATURE_NAME.Action, $FEATURE_NAME.Effect>(
	initialState = $FEATURE_NAME.State.Loading
) {
	fun $LOAD_METHOD_NAME() =
		sendAction($FEATURE_NAME.Action.$LOAD_ACTION_NAME)

	override suspend fun processAction(
		action: $FEATURE_NAME.Action,
		sideEffect: ($FEATURE_NAME.Effect) -> Unit
	): Flow<Mutation<$FEATURE_NAME.State>> {
		return when (action) {
			is $FEATURE_NAME.Action.$LOAD_ACTION_NAME ->
				$ACTION_PROCESSOR_PARAM_NAME.process(action, sideEffect)
		}
	}
}
