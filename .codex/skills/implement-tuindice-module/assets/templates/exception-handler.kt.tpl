package $PACKAGE.domain.usecase.exceptionhandler

import $PACKAGE.domain.usecase.error.$USE_CASE_ERROR_NAME
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.base.utils.extension.isTimeout
import com.gdavidpb.tuindice.base.utils.extension.isUnavailable

class $EXCEPTION_HANDLER_NAME(
	private val networkRepository: NetworkRepository,
	override val reportingRepository: ReportingRepository
) : ExceptionHandler<$USE_CASE_ERROR_NAME>() {
	override fun parseException(throwable: Throwable): $USE_CASE_ERROR_NAME? {
		return when {
			throwable.isUnavailable() -> $USE_CASE_ERROR_NAME.Unavailable
			throwable.isTimeout() -> $USE_CASE_ERROR_NAME.Timeout
			throwable.isConnection() -> $USE_CASE_ERROR_NAME.NoConnection(networkRepository.isAvailable())
			else -> null
		}
	}
}
