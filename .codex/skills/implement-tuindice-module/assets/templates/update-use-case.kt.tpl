package $PACKAGE.domain.usecase

import $PACKAGE.domain.repository.$REPOSITORY_INTERFACE_NAME
import $PACKAGE.domain.usecase.error.$UPDATE_USE_CASE_ERROR_NAME
import $PACKAGE.domain.usecase.exceptionhandler.$UPDATE_EXCEPTION_HANDLER_NAME
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class $UPDATE_USE_CASE_NAME(
	private val $REPOSITORY_PARAM_NAME: $REPOSITORY_INTERFACE_NAME,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: $UPDATE_EXCEPTION_HANDLER_NAME
) : FlowUseCase<Unit, Unit, $UPDATE_USE_CASE_ERROR_NAME>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
		$REPOSITORY_PARAM_NAME.updateMessage()

		return flowOf(Unit)
	}
}
