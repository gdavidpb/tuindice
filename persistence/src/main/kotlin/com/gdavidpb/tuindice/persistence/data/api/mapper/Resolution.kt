package com.gdavidpb.tuindice.persistence.data.api.mapper

import com.gdavidpb.tuindice.persistence.data.api.model.TransactionRequestAction
import com.gdavidpb.tuindice.persistence.data.api.model.TransactionRequestType
import com.gdavidpb.tuindice.persistence.data.api.response.ResolutionResponse
import com.gdavidpb.tuindice.persistence.domain.model.Resolution
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionAction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionType

fun ResolutionResponse.toResolution() = Resolution(
	uid = uid,
	localReference = localReference,
	remoteReference = remoteReference,
	type = type.toTransactionType(),
	action = action.toTransactionAction(),
	data = data
)

private fun TransactionRequestType.toTransactionType() = when (this) {
	TransactionRequestType.QUARTER -> TransactionType.QUARTER
	TransactionRequestType.SUBJECT -> TransactionType.SUBJECT
	TransactionRequestType.EVALUATION -> TransactionType.EVALUATION
}

private fun TransactionRequestAction.toTransactionAction() = when (this) {
	TransactionRequestAction.ADD -> TransactionAction.ADD
	TransactionRequestAction.UPDATE -> TransactionAction.UPDATE
	TransactionRequestAction.DELETE -> TransactionAction.DELETE
}