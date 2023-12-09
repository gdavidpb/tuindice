package com.gdavidpb.tuindice.transactions.data.repository.transactions.source.api.mapper

import com.gdavidpb.tuindice.transactions.data.repository.transactions.source.api.request.TransactionRequest
import com.gdavidpb.tuindice.transactions.domain.model.Transaction

fun Transaction.toTransactionRequest() = TransactionRequest(
	reference = reference,
	type = type.ordinal,
	action = action.ordinal,
	data = data
)