package com.gdavidpb.tuindice.transactions.data.api.mapper

import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionAction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionType
import com.gdavidpb.tuindice.transactions.data.api.model.transaction.TransactionRequestAction
import com.gdavidpb.tuindice.transactions.data.api.model.transaction.TransactionRequestType
import com.gdavidpb.tuindice.transactions.data.api.request.TransactionRequest

fun Transaction.toTransactionRequest() = TransactionRequest(
	reference = reference,
	type = type.toTransactionRequestType(),
	action = action.toTransactionRequestAction(),
	data = data
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