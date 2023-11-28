package com.gdavidpb.tuindice.record.data.repository.quarter.source.api.parser

import com.gdavidpb.tuindice.transactions.domain.model.Transaction
import com.gdavidpb.tuindice.transactions.domain.model.TransactionAction
import com.gdavidpb.tuindice.transactions.domain.model.TransactionType
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