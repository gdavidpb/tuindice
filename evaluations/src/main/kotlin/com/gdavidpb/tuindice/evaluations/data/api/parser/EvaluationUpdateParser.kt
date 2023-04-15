package com.gdavidpb.tuindice.evaluations.data.api.parser

import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionAction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionType
import com.gdavidpb.tuindice.base.utils.extension.bodyAs
import com.gdavidpb.tuindice.base.utils.extension.bodyToString
import com.gdavidpb.tuindice.base.utils.extension.findAnnotation
import com.gdavidpb.tuindice.evaluations.data.api.request.UpdateEvaluationRequest
import com.gdavidpb.tuindice.transactions.data.api.transaction.RequestParser
import okhttp3.Request
import retrofit2.http.PATCH

class EvaluationUpdateParser : RequestParser {
	override fun match(request: Request): Boolean {
		return request.findAnnotation<PATCH>()?.value == "evaluations"
	}

	override fun parse(request: Request): Transaction {
		val body = request.bodyAs<UpdateEvaluationRequest>()
		val json = request.bodyToString()

		requireNotNull(body)
		requireNotNull(json)

		return Transaction(
			reference = body.evaluationId,
			type = TransactionType.QUARTER,
			action = TransactionAction.DELETE,
			data = json
		)
	}
}