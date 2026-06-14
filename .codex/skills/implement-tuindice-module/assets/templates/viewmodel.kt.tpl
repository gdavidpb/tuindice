package $PACKAGE.presentation.viewmodel

import $PACKAGE.presentation.contract.$FEATURE_NAME
import $PACKAGE.presentation.machine.$MACHINE_NAME
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel

class $VIEWMODEL_NAME(
	override val screenMachine: $MACHINE_NAME,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<$FEATURE_NAME.State, $FEATURE_NAME.Action, $FEATURE_NAME.Effect>(
	name = "$MODULE_NAME",
	initialState = screenMachine.initialState(),
	initialAction = $FEATURE_NAME.Action.$OBSERVE_ACTION_NAME,
	dispatchers = dispatchers
) {
	fun $REFRESH_METHOD_NAME() =
		sendAction($FEATURE_NAME.Action.$REFRESH_ACTION_NAME)
}
