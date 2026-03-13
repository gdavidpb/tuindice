package $PACKAGE.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface $USE_CASE_ERROR_NAME : UseCaseError {
	data object Timeout : $USE_CASE_ERROR_NAME
	data object Unavailable : $USE_CASE_ERROR_NAME
	class NoConnection(val isNetworkAvailable: Boolean) : $USE_CASE_ERROR_NAME
}
