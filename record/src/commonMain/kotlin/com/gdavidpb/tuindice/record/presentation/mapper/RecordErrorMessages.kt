package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.mapper.commonNetworkUnavailableMessage
import com.gdavidpb.tuindice.base.presentation.mapper.commonServiceUnavailableMessage
import com.gdavidpb.tuindice.base.presentation.mapper.commonTimeoutMessage
import com.gdavidpb.tuindice.base.presentation.mapper.commonUnexpectedErrorMessage
import com.gdavidpb.tuindice.record.domain.model.RecordRejectionKind
import com.gdavidpb.tuindice.record.domain.usecase.error.RecordUseCaseError
import org.jetbrains.compose.resources.getString
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.snack_grade_rejected
import tuindice.record.generated.resources.snack_synthetic_term_rejected
import tuindice.record.generated.resources.snack_synthetic_terms_rejected

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

internal suspend fun recordRejectionMessage(kind: RecordRejectionKind, count: Int): String {
	return when {
		kind == RecordRejectionKind.Grade -> getString(Res.string.snack_grade_rejected)
		count > 1 -> getString(Res.string.snack_synthetic_terms_rejected, count)
		else -> getString(Res.string.snack_synthetic_term_rejected)
	}
}
