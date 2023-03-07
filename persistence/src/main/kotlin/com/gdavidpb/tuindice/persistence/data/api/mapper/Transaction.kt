package com.gdavidpb.tuindice.persistence.data.api.mapper

import com.gdavidpb.tuindice.persistence.data.api.model.SyncAction
import com.gdavidpb.tuindice.persistence.data.api.model.SyncType
import com.gdavidpb.tuindice.persistence.data.api.request.SyncTransactionRequest
import com.gdavidpb.tuindice.persistence.data.api.response.SyncResolutionResponse
import com.gdavidpb.tuindice.persistence.domain.model.*

fun Transaction<TransactionData>.toSyncTransactionRequest() = SyncTransactionRequest(
	reference = reference,
	type = type.toSyncType(),
	action = action.toSyncAction(),
	timestamp = timestamp,
	data = data
)

fun SyncResolutionResponse<*>.toTransactionResult() = TransactionResult(
	reference = reference
)

private fun TransactionType.toSyncType() = when (this) {
	TransactionType.QUARTER -> SyncType.QUARTER
	TransactionType.SUBJECT -> SyncType.SUBJECT
	TransactionType.EVALUATION -> SyncType.EVALUATION
}

private fun TransactionAction.toSyncAction() = when (this) {
	TransactionAction.ADD -> SyncAction.ADD
	TransactionAction.UPDATE -> SyncAction.UPDATE
	TransactionAction.DELETE -> SyncAction.DELETE
}