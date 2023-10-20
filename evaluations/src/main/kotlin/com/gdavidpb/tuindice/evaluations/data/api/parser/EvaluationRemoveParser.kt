package com.gdavidpb.tuindice.evaluations.data.api.parser

import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionAction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionType
import com.gdavidpb.tuindice.base.utils.extension.findAnnotation
import com.gdavidpb.tuindice.transactions.data.api.transaction.RequestParser
import okhttp3.Request
import retrofit2.http.DELETE

class EvaluationRemoveParser : RequestParser {
	override fun match(request: Request): Boolean {
		return request.findAnnotation<DELETE>()?.value == "evaluations"
	}

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