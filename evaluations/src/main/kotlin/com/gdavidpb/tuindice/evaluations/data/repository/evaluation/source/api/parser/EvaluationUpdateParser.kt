package com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.parser

import com.gdavidpb.tuindice.base.utils.extension.bodyAs
import com.gdavidpb.tuindice.base.utils.extension.bodyToString
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.request.UpdateEvaluationRequest
import com.gdavidpb.tuindice.transactions.data.repository.transactions.source.api.retrofit.TransactionParser
import com.gdavidpb.tuindice.transactions.domain.model.Transaction
import com.gdavidpb.tuindice.transactions.domain.model.TransactionAction
import com.gdavidpb.tuindice.transactions.domain.model.TransactionType
import okhttp3.Request

class EvaluationUpdateParser : TransactionParser {
	override fun parse(request: Request): Transaction {
		val body = request.bodyAs<UpdateEvaluationRequest>()
		val json = request.bodyToString()

		requireNotNull(body)
		requireNotNull(json)

		return Transaction(
			reference = body.evaluationId,
			type = TransactionType.QUARTER,
			action = TransactionAction.UPDATE,
			data = json
		)
	}
}