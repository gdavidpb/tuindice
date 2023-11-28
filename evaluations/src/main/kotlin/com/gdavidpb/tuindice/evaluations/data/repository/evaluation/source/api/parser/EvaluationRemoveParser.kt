package com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.parser

import com.gdavidpb.tuindice.transactions.domain.model.Transaction
import com.gdavidpb.tuindice.transactions.domain.model.TransactionAction
import com.gdavidpb.tuindice.transactions.domain.model.TransactionType
import com.gdavidpb.tuindice.transactions.data.repository.transactions.source.api.retrofit.TransactionParser
import okhttp3.Request

class EvaluationRemoveParser : TransactionParser {
	override fun parse(request: Request): Transaction {
		val evaluationId = request.url.queryParameter("eid")

		requireNotNull(evaluationId)

		return Transaction(
			reference = evaluationId,
			type = TransactionType.EVALUATION,
			action = TransactionAction.DELETE
		)
	}
}