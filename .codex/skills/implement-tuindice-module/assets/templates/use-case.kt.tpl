package $PACKAGE.domain.usecase

import $PACKAGE.domain.repository.$REPOSITORY_INTERFACE_NAME
import $PACKAGE.domain.usecase.error.$USE_CASE_ERROR_NAME
import $PACKAGE.domain.usecase.exceptionhandler.$EXCEPTION_HANDLER_NAME
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow

class $LOAD_USE_CASE_NAME(
	private val $REPOSITORY_PARAM_NAME: $REPOSITORY_INTERFACE_NAME,
	override val exceptionHandler: $EXCEPTION_HANDLER_NAME
) : FlowUseCase<Unit, String, $USE_CASE_ERROR_NAME>() {
	override suspend fun executeOnBackground(params: Unit): Flow<String> {
		return $REPOSITORY_PARAM_NAME.getMessageFlow()
	}
}
