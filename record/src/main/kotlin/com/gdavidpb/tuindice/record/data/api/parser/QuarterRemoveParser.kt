package com.gdavidpb.tuindice.record.data.api.parser

import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionAction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionType
import com.gdavidpb.tuindice.base.utils.extension.bodyAs
import com.gdavidpb.tuindice.base.utils.extension.bodyToString
import com.gdavidpb.tuindice.base.utils.extension.findAnnotation
import com.gdavidpb.tuindice.record.data.api.request.RemoveQuarterRequest
import com.gdavidpb.tuindice.transactions.data.retrofit.RequestParser
import okhttp3.Request
import retrofit2.http.DELETE

class QuarterRemoveParser : RequestParser {
	override fun match(request: Request): Boolean {
		return request.findAnnotation<DELETE>()?.value == "quarters"
	}

	override fun parse(request: Request): Transaction {
		val body = request.bodyAs<RemoveQuarterRequest>()
		val json = request.bodyToString()

		requireNotNull(body)
		requireNotNull(json)

		return Transaction.Builder()
			.withReference(body.quarterId)
			.withType(TransactionType.QUARTER)
			.withAction(TransactionAction.DELETE)
			.withData(json)
			.build()
	}
}