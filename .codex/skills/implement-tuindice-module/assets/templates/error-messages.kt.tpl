package $PACKAGE.presentation.mapper

import $PACKAGE.domain.usecase.error.$OBSERVE_USE_CASE_ERROR_NAME
import $PACKAGE.domain.usecase.error.$UPDATE_USE_CASE_ERROR_NAME
import org.jetbrains.compose.resources.getString
import $GENERATED_RESOURCES_PACKAGE.Res

internal suspend fun $OBSERVE_USE_CASE_ERROR_NAME?.toErrorMessage(): String {
	return getString(Res.string.snack_default_error)
}

internal suspend fun $UPDATE_USE_CASE_ERROR_NAME?.toErrorMessage(): String {
	return when (this) {
		is $UPDATE_USE_CASE_ERROR_NAME.NoConnection ->
			if (isNetworkAvailable)
				getString(Res.string.snack_service_unavailable)
			else
				getString(Res.string.snack_network_unavailable)

		is $UPDATE_USE_CASE_ERROR_NAME.Timeout ->
			getString(Res.string.snack_timeout)

		is $UPDATE_USE_CASE_ERROR_NAME.Unavailable ->
			getString(Res.string.snack_service_unavailable)

		else ->
			getString(Res.string.snack_default_error)
	}
}
