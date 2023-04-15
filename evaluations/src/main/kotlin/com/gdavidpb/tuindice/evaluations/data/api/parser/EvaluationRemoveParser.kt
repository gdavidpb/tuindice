package com.gdavidpb.tuindice.evaluations.data.api.parser

import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionAction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionType
import com.gdavidpb.tuindice.base.utils.extension.bodyAs
import com.gdavidpb.tuindice.base.utils.extension.bodyToString
import com.gdavidpb.tuindice.base.utils.extension.findAnnotation
import com.gdavidpb.tuindice.evaluations.data.api.request.RemoveEvaluationRequest
import com.gdavidpb.tuindice.transactions.data.api.transaction.RequestParser
import okhttp3.Request
import retrofit2.http.DELETE

class EvaluationRemoveParser : RequestParser {
	override fun match(request: Request): Boolean {
		return request.findAnnotation<DELETE>()?.value == "evaluations"
	}

	override fun parse(request: Request): Transaction {
		val body = request.bodyAs<RemoveEvaluationRequest>()
		val json = request.bodyToString()

		requireNotNull(body)
		requireNotNull(json)

		return Transaction(
			reference = body.evaluationId,
			type = TransactionType.EVALUATION,
			action = TransactionAction.DELETE,
			data = json
		)
	}
}