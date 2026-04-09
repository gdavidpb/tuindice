package com.gdavidpb.tuindice.record.presentation.action

import com.gdavidpb.tuindice.record.domain.usecase.error.RecordUseCaseError
import com.gdavidpb.tuindice.record.presentation.contract.Record
import org.jetbrains.compose.resources.getString
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.snack_default_error
import tuindice.record.generated.resources.snack_network_unavailable
import tuindice.record.generated.resources.snack_service_unavailable
import tuindice.record.generated.resources.snack_timeout

internal suspend fun sendRecordErrorEffect(
	error: RecordUseCaseError?,
	sideEffect: (Record.Effect) -> Unit
) {
	when (error) {
		RecordUseCaseError.Unauthorized ->
			sideEffect(Record.Effect.NavigateToOutdatedCredentials)

		RecordUseCaseError.Timeout ->
			sideEffect(Record.Effect.ShowSnackBar(getString(Res.string.snack_timeout)))

		RecordUseCaseError.Unavailable ->
			sideEffect(Record.Effect.ShowSnackBar(getString(Res.string.snack_service_unavailable)))

		RecordUseCaseError.NoConnection ->
			sideEffect(Record.Effect.ShowSnackBar(getString(Res.string.snack_network_unavailable)))

		null ->
			sideEffect(Record.Effect.ShowSnackBar(getString(Res.string.snack_default_error)))
	}
}
