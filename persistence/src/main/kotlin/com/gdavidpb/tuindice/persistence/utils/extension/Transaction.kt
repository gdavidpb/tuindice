package com.gdavidpb.tuindice.persistence.utils.extension

import com.gdavidpb.tuindice.persistence.data.api.response.SubjectDataResponse
import com.gdavidpb.tuindice.persistence.domain.model.*

fun createInProgressTransaction(
	reference: String,
	type: TransactionType,
	action: TransactionAction,
	data: TransactionData? = null
): Transaction {
	if (type == TransactionType.SUBJECT) require(data is SubjectDataResponse)

	return Transaction(
		reference = reference,
		type = type,
		action = action,
		status = TransactionStatus.IN_PROGRESS,
		timestamp = System.currentTimeMillis(),
		data = data
	)
}