package $PACKAGE.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface $UPDATE_USE_CASE_ERROR_NAME : UseCaseError {
	data object Timeout : $UPDATE_USE_CASE_ERROR_NAME
	data object Unavailable : $UPDATE_USE_CASE_ERROR_NAME
	class NoConnection(val isNetworkAvailable: Boolean) : $UPDATE_USE_CASE_ERROR_NAME
}
