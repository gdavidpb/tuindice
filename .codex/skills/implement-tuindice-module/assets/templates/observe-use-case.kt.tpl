package $PACKAGE.domain.usecase

import $PACKAGE.domain.repository.$REPOSITORY_INTERFACE_NAME
import $PACKAGE.domain.usecase.error.$OBSERVE_USE_CASE_ERROR_NAME
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow

class $OBSERVE_USE_CASE_NAME(
	private val $REPOSITORY_PARAM_NAME: $REPOSITORY_INTERFACE_NAME
) : FlowUseCase<Unit, String, $OBSERVE_USE_CASE_ERROR_NAME>() {
	override suspend fun executeOnBackground(params: Unit): Flow<String> {
		return $REPOSITORY_PARAM_NAME.observeMessageFlow()
	}
}
