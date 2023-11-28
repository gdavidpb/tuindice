package com.gdavidpb.tuindice.record.data.repository.quarter.source.api.parser

import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionAction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionType
import com.gdavidpb.tuindice.transactions.data.repository.transactions.source.api.retrofit.TransactionParser
import okhttp3.Request

class QuarterRemoveParser : TransactionParser {
	override fun parse(request: Request): Transaction {
		val quarterId = request.url.queryParameter("qid")

		requireNotNull(quarterId)

		return Transaction(
			reference = quarterId,
			type = TransactionType.QUARTER,
			action = TransactionAction.DELETE
		)
	}
}