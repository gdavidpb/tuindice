package com.gdavidpb.tuindice.record.data.repository.quarter.source.api.parser

import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionAction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionType
import com.gdavidpb.tuindice.base.utils.extension.findAnnotation
import com.gdavidpb.tuindice.transactions.data.api.transaction.RequestParser
import okhttp3.Request
import retrofit2.http.DELETE

class QuarterRemoveParser : RequestParser {
	override fun match(request: Request): Boolean {
		return request.findAnnotation<DELETE>()?.value == "quarters"
	}

	override fun parse(request: Request): Transaction {
		val quarterId = request.url.queryParameter("qid")

		requireNotNull(quarterId)

		return Transaction(
			reference = quarterId,
			type = TransactionType.QUARTER,
			action = TransactionAction.DELETE,
			data = ""
		)
	}
}