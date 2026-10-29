package com.gdavidpb.tuindice.assistant.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface ExtractEvaluationsFromPhotoError : UseCaseError {
	/** [retryAfterSeconds] is the wait the server gave, when it gave one. */
	data class AssistantDisabled(val retryAfterSeconds: Int?) : ExtractEvaluationsFromPhotoError

	data object ConsentRequired : ExtractEvaluationsFromPhotoError

	/** [retryAfterSeconds] is the wait the server gave before the quota comes back, when it gave one. */
	data class QuotaExhausted(val retryAfterSeconds: Int?) : ExtractEvaluationsFromPhotoError

	data object Restricted : ExtractEvaluationsFromPhotoError

	data object GuidedOnly : ExtractEvaluationsFromPhotoError

	data object Other : ExtractEvaluationsFromPhotoError
}
