package com.gdavidpb.tuindice.persistence.utils.extension

import com.gdavidpb.tuindice.persistence.data.api.model.data.SubjectData
import com.gdavidpb.tuindice.persistence.domain.model.*

fun createInProgressTransaction(
	reference: String,
	type: TransactionType,
	action: TransactionAction,
	data: TransactionData? = null
): Transaction {
	if (type == TransactionType.SUBJECT) require(data is SubjectData)

	return Transaction(
		id = "$action:$reference",
		reference = reference,
		type = type,
		action = action,
		status = TransactionStatus.IN_PROGRESS,
		timestamp = System.currentTimeMillis(),
		data = data
	)
}