package com.gdavidpb.tuindice.persistence.data.api.mapper

import com.gdavidpb.tuindice.base.domain.model.subject.SubjectUpdateTransaction
import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionAction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionOperation
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionType
import com.gdavidpb.tuindice.persistence.data.api.model.transaction.TransactionRequestAction
import com.gdavidpb.tuindice.persistence.data.api.model.transaction.TransactionRequestType
import com.gdavidpb.tuindice.persistence.data.api.request.TransactionRequest

fun <T : TransactionOperation> Transaction<T>.toTransactionRequest() =
	TransactionRequest(
		reference = reference,
		type = type.toTransactionRequestType(),
		action = action.toTransactionRequestAction(),
		timestamp = timestamp,
		operation = operation.toTransactionRequest()
	)

private fun TransactionType.toTransactionRequestType() = when (this) {
	TransactionType.QUARTER -> TransactionRequestType.QUARTER
	TransactionType.SUBJECT -> TransactionRequestType.SUBJECT
	TransactionType.EVALUATION -> TransactionRequestType.EVALUATION
}

private fun TransactionAction.toTransactionRequestAction() = when (this) {
	TransactionAction.ADD -> TransactionRequestAction.ADD
	TransactionAction.UPDATE -> TransactionRequestAction.UPDATE
	TransactionAction.DELETE -> TransactionRequestAction.DELETE
}

private fun TransactionOperation.toTransactionRequest() =
	when (this) {
		is SubjectUpdateTransaction -> toSubjectUpdateRequest()
		else -> throw NoWhenBranchMatchedException()
	}