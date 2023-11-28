package com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.parser

import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionAction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionType
import com.gdavidpb.tuindice.transactions.data.api.transaction.TransactionParser
import okhttp3.Request

class EvaluationRemoveParser : TransactionParser {
	override fun parse(request: Request): Transaction {
		val evaluationId = request.url.queryParameter("eid")

		requireNotNull(evaluationId)

		return Transaction(
			reference = evaluationId,
			type = TransactionType.EVALUATION,
			action = TransactionAction.DELETE,
			data = ""
		)
	}
}