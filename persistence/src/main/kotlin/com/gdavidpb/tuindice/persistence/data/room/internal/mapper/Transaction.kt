package com.gdavidpb.tuindice.persistence.data.room.internal.mapper

import com.gdavidpb.tuindice.base.domain.model.quarter.QuarterRemoveTransaction
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectUpdateTransaction
import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionData
import com.gdavidpb.tuindice.persistence.data.room.entity.TransactionEntity
import com.gdavidpb.tuindice.persistence.utils.fromJson
import com.gdavidpb.tuindice.persistence.utils.toJson
import com.google.gson.Gson

fun Transaction<*>.toTransactionEntity(gson: Gson) = TransactionEntity(
	id = "$action:$reference",
	accountId = uid,
	reference = reference,
	type = type,
	action = action,
	timestamp = timestamp,
	data = data.toJson(gson)
)

fun TransactionEntity.toTransaction(gson: Gson) = Transaction.Builder<TransactionData>()
	.withReference(reference)
	.withTimestamp(timestamp)
	.withData(resolveData(gson))
	.build()

private fun TransactionEntity.resolveData(gson: Gson) = when {
	isSubjectUpdate() ->
		data.fromJson<SubjectUpdateTransaction>(gson)
	isQuarterRemove() ->
		data.fromJson<QuarterRemoveTransaction>(gson)
	else -> throw NoWhenBranchMatchedException()
}