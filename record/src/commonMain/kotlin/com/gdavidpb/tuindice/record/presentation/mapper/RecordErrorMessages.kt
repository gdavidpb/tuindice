package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.mapper.commonNetworkUnavailableMessage
import com.gdavidpb.tuindice.base.presentation.mapper.commonServiceUnavailableMessage
import com.gdavidpb.tuindice.base.presentation.mapper.commonTimeoutMessage
import com.gdavidpb.tuindice.base.presentation.mapper.commonUnexpectedErrorMessage
import com.gdavidpb.tuindice.record.domain.usecase.error.RecordUseCaseError

internal suspend fun RecordUseCaseError?.toRecordFailureMessage(): String {
	return when (this) {
		RecordUseCaseError.NoConnection ->
			commonNetworkUnavailableMessage()

		RecordUseCaseError.Timeout ->
			commonTimeoutMessage()

		RecordUseCaseError.Unavailable ->
			commonServiceUnavailableMessage()

		else ->
			commonUnexpectedErrorMessage()
	}
}
