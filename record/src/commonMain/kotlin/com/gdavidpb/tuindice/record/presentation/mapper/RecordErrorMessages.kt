package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.record.domain.usecase.error.RecordUseCaseError
import org.jetbrains.compose.resources.getString
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.snack_default_error
import tuindice.record.generated.resources.snack_network_unavailable
import tuindice.record.generated.resources.snack_service_unavailable
import tuindice.record.generated.resources.snack_timeout

internal suspend fun RecordUseCaseError?.toRecordFailureMessage(): String {
	return when (this) {
		RecordUseCaseError.NoConnection ->
			getString(Res.string.snack_network_unavailable)

		RecordUseCaseError.Timeout ->
			getString(Res.string.snack_timeout)

		RecordUseCaseError.Unavailable ->
			getString(Res.string.snack_service_unavailable)

		else ->
			getString(Res.string.snack_default_error)
	}
}
