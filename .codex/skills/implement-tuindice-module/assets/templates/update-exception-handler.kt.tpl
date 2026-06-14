package $PACKAGE.domain.usecase.exceptionhandler

import $PACKAGE.domain.usecase.error.$UPDATE_USE_CASE_ERROR_NAME
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.base.utils.extension.isTimeout
import com.gdavidpb.tuindice.base.utils.extension.isUnavailable

class $UPDATE_EXCEPTION_HANDLER_NAME(
	private val networkRepository: NetworkRepository,
	override val reportingRepository: ReportingRepository
) : ExceptionHandler<$UPDATE_USE_CASE_ERROR_NAME> {
	override fun parseException(throwable: Throwable): $UPDATE_USE_CASE_ERROR_NAME? {
		return when {
			throwable.isUnavailable() -> $UPDATE_USE_CASE_ERROR_NAME.Unavailable
			throwable.isTimeout() -> $UPDATE_USE_CASE_ERROR_NAME.Timeout
			throwable.isConnection() ->
				$UPDATE_USE_CASE_ERROR_NAME.NoConnection(networkRepository.isAvailable())

			else -> null
		}
	}
}
